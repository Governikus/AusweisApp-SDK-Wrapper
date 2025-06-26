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



job("SDKWrapper_Docs_Review") {
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
			trigger('SDKWrapper_Docs_Build') {
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
					predefinedProp('sdkSource', '${REVIEWBOARD_REVIEW_BRANCH}_Docs')
				}
			}
		}
		copyArtifacts('SDKWrapper_Docs_Build') {
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
	job("SDKWrapper_Docs_Daily_${branch}") {
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
				trigger('SDKWrapper_Docs_Build') {
					block {
						buildStepFailure('FAILURE')
						failure('FAILURE')
						unstable('UNSTABLE')
					}
					parameters {
						predefinedProp('REVIEWBOARD_REVIEW_BRANCH', "${branch}")
						predefinedProp('sdkSource', "${branch}_Docs")
					}
				}
			}
			copyArtifacts('SDKWrapper_Docs_Build') {
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

job("SDKWrapper_Docs_Release") {
	label('Common')
	parameters {
		stringParam( 'changeset', '', 'Build given changeset (tag) as release' )
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
		buildDescription('', 'Release_Docs')
		downstreamParameterized {
			trigger('SDKWrapper_Docs_Build') {
				block {
					buildStepFailure('FAILURE')
					failure('FAILURE')
					unstable('UNSTABLE')
				}
				parameters {
					predefinedProp('REVIEWBOARD_REVIEW_BRANCH', '$changeset')
					predefinedProp('sdkSource', 'Release_Docs')
				}
			}
		}
		copyArtifacts('SDKWrapper_Docs_Build') {
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

pipelineJob('SDKWrapper_Docs_Build') {
	logRotator {
		daysToKeep(7)
		numToKeep(50)
	}
	definition {
		cps {
			script(readFileFromWorkspace('docs/jenkins/PipelineBuild.groovy'))
			sandbox()
		}
	}
}
