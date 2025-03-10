pipeline {
	agent {
		label 'Docs'
	}
	parameters {
		string( name: 'REVIEWBOARD_REVIEW_ID', defaultValue: '', description: 'ID of the Review' )
		string( name: 'REVIEWBOARD_REVIEW_BRANCH', defaultValue: 'default', description: 'Branch/Revision' )
		string( name: 'REVIEWBOARD_SERVER', defaultValue: '', description: 'Server' )
		string( name: 'REVIEWBOARD_STATUS_UPDATE_ID', defaultValue: '', description: '' )
		string( name: 'REVIEWBOARD_DIFF_REVISION', defaultValue: '', description: '' )
		string( name: 'sdkSource', defaultValue: 'Release_Docs', description: 'Source of the SDK.\nExamples: Release_Docs, default_Review_Docs, default_Docs' )
	}
	options {
		skipStagesAfterUnstable()
		disableConcurrentBuilds()
		timeout(time: 30, unit: 'MINUTES')
	}
	stages {
		stage('Cleanup') {
			steps {
				cleanWs(
					deleteDirs: true,
					disableDeferredWipeout: true
				)
			}
		}
		stage('Checkout') {
			steps {
				checkout( [
					$class : 'MercurialSCM',
					revisionType: 'TAG',
					revision: "${params.REVIEWBOARD_REVIEW_BRANCH}",
					clean  : true,
					source : 'https://hg.governikus.de/AusweisApp/SDKWrapper'
				] )
			}
		}
		stage('Patch') {
			when { expression { params.REVIEWBOARD_REVIEW_ID != '' } }
			steps {
				publishReview downloadOnly: true, installRBTools: false
				sh "hg --config patch.eol=auto import --no-commit patch.diff"
			}
		}
		stage('Source') {
			steps {
				sh 'cmake -E tar cvfJ SDKWrapper-Docs-Sources.tar.xz docs README.md LICENSE.txt LICENSE.officially.txt'
			}
		}
		stage('Pull AA2 SDK Doku') {
			steps {
				script {
					currentBuild.description = "${params.sdkSource}"
				}
				copyArtifacts projectName: "${params.sdkSource}", filter: '**/en/objects.inv', flatten: true, selector: lastSuccessful(), target: 'docs'
			}
		}
		stage('Build') {
			steps {
				dir('docs') {
					sh 'make html'
				}
			}
		}
		stage('Package') {
			steps {
				dir('docs') {
					sh 'cd _build/; mv html SDKWrapper-Docs; cmake -E tar cvfJ SDKWrapper-Docs.tar.xz SDKWrapper-Docs'
				}
			}
		}
		stage('Archive') {
			steps {
				archiveArtifacts 'SDKWrapper-Docs-Sources.tar.xz'
				archiveArtifacts 'docs/_build/SDKWrapper-Docs.tar.xz'
			}
		}
	}

	post {
		always {
			script {
				if (params.REVIEWBOARD_REVIEW_ID != '') {
					def rb_result = "error"
					def rb_desc = "build failed."
					if (currentBuild.result == 'SUCCESS') {
						rb_result = "done-success"
						rb_desc = "build succeeded."
					}

					withCredentials([string(credentialsId: 'RBToken', variable: 'RBToken')]) {
						sh "rbt status-update set --state ${rb_result} --description '${rb_desc}' -r ${params.REVIEWBOARD_REVIEW_ID} -s ${params.REVIEWBOARD_STATUS_UPDATE_ID} --server ${params.REVIEWBOARD_SERVER} --username jenkins --api-token $RBToken"
					}
				}
			}
		}
	}
}
