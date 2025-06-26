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



job("SDKWrapper_Android_Review") {
	label('Common')
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
			trigger('SDKWrapper_Android_Build') {
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
					predefinedProp('sdkSource', '${REVIEWBOARD_REVIEW_BRANCH}_Android_AAR')
					predefinedProp('performSonarScan', "true")
				}
			}
		}
		copyArtifacts('SDKWrapper_Android_Build') {
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
	job("SDKWrapper_Android_Daily_${branch}") {
		label('Common')
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
				trigger('SDKWrapper_Android_Build') {
					block {
						buildStepFailure('FAILURE')
						failure('FAILURE')
						unstable('UNSTABLE')
					}
					parameters {
						predefinedProp('REVIEWBOARD_REVIEW_BRANCH', "${branch}")
						predefinedProp('sdkSource', "${branch}_Android_AAR")
						predefinedProp('performSonarScan', "true")
						predefinedProp('publish', "snapshot")
					}
				}
			}
			copyArtifacts('SDKWrapper_Android_Build') {
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
			mailer('${DEFAULT_EMAIL_RECIPIENTS}', true, false)
		}
		triggers {
			cron('0 6 * * *')
		}
	}
}

job("SDKWrapper_Android_Release") {
	label('Common')
	parameters {
		stringParam( 'changeset', '', 'Build given changeset (tag) as release' )
		choiceParam( 'sdkSource', ['maven', 'Release_Android_AAR'], 'Source of the AAR.')
		reactiveChoice {
			name ('actions')
			description('Upload to maven central repository')
			filterable(false)
			choiceType('PT_CHECKBOX')
			script {
				groovyScript {
					script {
						script("if (sdkSource.equals('maven')) { return ['release:selected', 'central'] } else { return ['release:selected', 'central:disabled'] }")
						sandbox(true)
					}
					fallbackScript {
						script("return ['SCRIPT ERROR:disabled']")
						sandbox(true)
					}
				}
			}
			referencedParameters('sdkSource')
			randomName('')
			filterLength(0)
		}
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
			trigger('SDKWrapper_Android_Build') {
				block {
					buildStepFailure('FAILURE')
					failure('FAILURE')
					unstable('UNSTABLE')
				}
				parameters {
					predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '$changeset')
					predefinedProp('sdkSource', '$sdkSource')
					predefinedProp('publish', '$actions')
				}
			}
		}
		copyArtifacts('SDKWrapper_Android_Build') {
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

pipelineJob('SDKWrapper_Android_Build') {
	logRotator {
		daysToKeep(7)
		numToKeep(50)
	}
	definition {
		cps {
			script(readFileFromWorkspace('android/jenkins/PipelineBuild.groovy'))
			sandbox()
		}
	}
}
