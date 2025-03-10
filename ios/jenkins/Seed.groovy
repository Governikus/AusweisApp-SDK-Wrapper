println 'Try to slurp branches from repository'

def branches = []
def api = new URL("${MERCURIAL_REPOSITORY_URL}/json-branches/")
def content = new groovy.json.JsonSlurper().parse(api.newReader())
content.each
{
	empty, entry -> entry.each
	{
		if(it.status != 'closed')
		{
			branches << it.branch
		}
	}
}
if(branches.isEmpty())
{
	throw new Exception('Cannot find any branch')
}



job("SDKWrapper_iOS_Review_Trigger") {
	label('Trigger')
	logRotator {
		daysToKeep(3)
		numToKeep(50)
	}
	parameters {
		stringParam('REVIEWBOARD_SERVER', '', '')
		stringParam('REVIEWBOARD_REVIEW_ID', '', '')
		stringParam('REVIEWBOARD_REVIEW_BRANCH', '', '')
		stringParam('REVIEWBOARD_DIFF_REVISION', '', '')
		stringParam('REVIEWBOARD_STATUS_UPDATE_ID', '', '')
	}
	wrappers {
		preBuildCleanup() {
			deleteDirectories(true)
		}
	}
	steps {
		buildDescription('', '${REVIEWBOARD_REVIEW_ID} / ${REVIEWBOARD_DIFF_REVISION}')
		downstreamParameterized {
			trigger('SDKWrapper_iOS_Build') {
				block {
					buildStepFailure('FAILURE')
					failure('FAILURE')
					unstable('UNSTABLE')
				}
				parameters {
					predefinedProp('REVIEWBOARD_SERVER', '${REVIEWBOARD_SERVER}')
					predefinedProp('REVIEWBOARD_REVIEW_ID', '${REVIEWBOARD_REVIEW_ID}')
					predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '${REVIEWBOARD_REVIEW_BRANCH}')
					predefinedProp('REVIEWBOARD_DIFF_REVISION', '${REVIEWBOARD_DIFF_REVISION}')
					predefinedProp('REVIEWBOARD_STATUS_UPDATE_ID', '${REVIEWBOARD_STATUS_UPDATE_ID}')
					predefinedProp('sdkSource', '${REVIEWBOARD_REVIEW_BRANCH}_iOS_SwiftPackage')
					predefinedProp('performSonarScan', 'true')
				}
			}
		}
		copyArtifacts('SDKWrapper_iOS_Build') {
			buildSelector {
				workspace()
			}
		}
	}
	publishers {
		archiveArtifacts {
			onlyIfSuccessful(false)
			pattern('**/*')
		}
	}
}

branches.each { branch ->
	job("SDKWrapper_iOS_Daily_${branch}") {
		label('Trigger')
		logRotator {
			daysToKeep(7)
			numToKeep(10)
		}
		wrappers {
			preBuildCleanup() {
				deleteDirectories(true)
			}
		}
		steps {
			downstreamParameterized {
				trigger('SDKWrapper_iOS_Build') {
					block {
						buildStepFailure('FAILURE')
						failure('FAILURE')
						unstable('UNSTABLE')
					}
					parameters {
						predefinedProp('REVIEWBOARD_REVIEW_BRANCH', "${branch}")
						predefinedProp('sdkSource', "${branch}_iOS_SwiftPackage")
						predefinedProp('performSonarScan', "true")
					}
				}
			}
			copyArtifacts('SDKWrapper_iOS_Build') {
				buildSelector {
					workspace()
				}
			}
		}
		publishers {
			archiveArtifacts {
				allowEmpty(false)
				pattern('**/*')
			}
			mailer('', true, true)
		}
		triggers {
			cron('0 6 * * *')
		}
	}
}

job("SDKWrapper_iOS_Release") {
	label('Trigger')
	parameters {
		stringParam( 'changeset', '', 'Build given changeset (tag) as release' )
		choiceParam( 'sdkSource', ['github', 'Release_iOS_SwiftPackage'], 'Source of the AA2 Swift Package.')
	}
	wrappers {
		preBuildCleanup() {
			deleteDirectories(true)
		}
	}
	steps {
		wrappers {
			buildName('${changeset}')
		}
		buildDescription('', '${sdkSource}')
		downstreamParameterized {
			trigger('SDKWrapper_iOS_Build') {
				block {
					buildStepFailure('FAILURE')
					failure('FAILURE')
					unstable('UNSTABLE')
				}
				parameters {
					predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '$changeset')
					predefinedProp('sdkSource', '$sdkSource')
				}
			}
		}
		copyArtifacts('SDKWrapper_iOS_Build') {
			buildSelector {
				workspace()
			}
		}
	}
	publishers {
		archiveArtifacts {
			allowEmpty(false)
			pattern('**/*')
		}
	}
}

pipelineJob('SDKWrapper_iOS_Build') {
	logRotator {
		daysToKeep(7)
		numToKeep(50)
	}
	definition {
		cps {
			script(readFileFromWorkspace('ios/jenkins/PipelineBuild.groovy'))
			sandbox()
		}
	}
}
