pipeline {
	agent {
		label 'Android'
	}
	parameters {
		string( name: 'REVIEWBOARD_REVIEW_ID', defaultValue: '', description: 'ID of the Review' )
		string( name: 'REVIEWBOARD_REVIEW_BRANCH', defaultValue: 'default', description: 'Branch/Revision' )
		string( name: 'REVIEWBOARD_SERVER', defaultValue: '', description: 'Server' )
		string( name: 'REVIEWBOARD_STATUS_UPDATE_ID', defaultValue: '', description: '' )
		string( name: 'REVIEWBOARD_DIFF_REVISION', defaultValue: '', description: '' )
		string( name: 'sdkSource', defaultValue: 'maven', description: 'Source of the AAR.\nExamples: maven, Release_Android_AAR, default_Review_Android_AAR, default_Android_AAR' )
		booleanParam( name: 'performSonarScan', defaultValue: false, description: 'Perform a sonar scan')
		string( name: 'publish', defaultValue: '', description: 'Publish (snapshot | release | central) repository')
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
		stage('Copy AAR') {
			when {
				expression {
					return params.sdkSource != 'maven';
				}
			}

			steps {
				dir('android') {
					copyArtifacts(
						projectName: "${params.sdkSource}",
						filter: '**/*.aar',
						flatten: true,
						target: './ausweisapp',
						selector: lastSuccessful()
					)
					script {
						currentBuild.description = "${params.sdkSource}"
						sh 'mv ./ausweisapp/ausweisapp*.aar ./ausweisapp/ausweisapp.aar'
					}
				}
			}
		}
		stage('Static analysis') {
			steps {
				dir('android') {
					sh './gradlew lintKotlin'
					sh './gradlew lint'
					recordIssues (
						tool: androidLintParser(pattern: '**/lint-results*.xml'),
						qualityGates: [[threshold: 1, type: 'TOTAL', unstable: false]]
					)
				}
			}
		}
		stage('Compile') {
			steps {
				dir('android') {
					sh './gradlew compileDebugSources'
				}
			}
		}
		stage('Unit test') {
			steps {
				dir('android') {
					sh './gradlew test'
					sh './gradlew koverXmlReport'
					junit '**/TEST-*.xml'
				}
			}
		}
		stage('Sonar') {
			when { expression { params.performSonarScan } }
			steps {
				dir('android') {
					script {
						def PROJECT_VERSION = sh(
							script: 'hg log --template "{latesttag}" --rev .',
							returnStdout: true
						)
						def pullRequestParams = params.REVIEWBOARD_REVIEW_ID != '' ? '-Dsonar.pullrequest.key=${REVIEWBOARD_REVIEW_ID} -Dsonar.pullrequest.branch=${REVIEWBOARD_REVIEW_BRANCH} -Dsonar.pullrequest.base=${REVIEWBOARD_REVIEW_BRANCH}' : '-Dsonar.branch.name=${REVIEWBOARD_REVIEW_BRANCH}'
						sh "./gradlew sonar -Dsonar.scanner.metadataFilePath=${WORKSPACE}/tmp/sonar-metadata.txt ${pullRequestParams} -Dsonar.projectVersion=${PROJECT_VERSION} -Dsonar.token=${SONARQUBE_TOKEN}"
					}
				}
			}
		}
		stage('Package') {
			steps {
				dir('android') {
					sh './gradlew assemble'
					sh "./gradlew -Dmaven.repo.local=${WORKSPACE}/android/dist publishReleasePublicationToMavenLocal"
					sh "rm ${WORKSPACE}/android/dist/com/governikus/ausweisapp/sdkwrapper/maven-metadata-local.xml"
					sh "cd ${WORKSPACE}/android/dist; cmake -E tar cvfJ SDKWrapper-${REVIEWBOARD_REVIEW_BRANCH}-Android.tar.xz com"
				}
			}
		}
		stage('Publish snapshot') {
			when { expression { params.publish.contains('snapshot') } }
			steps {
				dir('android') {
					sh './gradlew publishSnapshotPublicationToNexusSnapshotRepository'
				}
			}
		}
		stage('Publish release') {
			when { expression { params.publish.contains('release') } }
			steps {
				dir('android') {
					sh './gradlew publishReleasePublicationToNexusReleaseRepository'
				}
			}
		}
		stage('Publish Maven') {
			when {
				expression { params.publish.contains('central') }
				equals expected: 'maven', actual: params.sdkSource
			}
			steps {
				dir('android') {
					sh './gradlew publishReleasePublicationToCentralRepository'
				}
			}
		}
		stage('Tarball') {
			steps {
				dir('android') {
					sh './gradlew tarball'
				}
			}
		}
		stage('Archive') {
			steps {
				dir('android') {
					archiveArtifacts artifacts: 'tester/build/outputs/apk/debug/sdkwrapper-tester-debug-*.apk'
					archiveArtifacts artifacts: 'tester/build/outputs/apk/release/sdkwrapper-tester-release-*.apk'
					archiveArtifacts artifacts: 'sdkwrapper/build/outputs/aar/sdkwrapper-debug-*.aar'
					archiveArtifacts artifacts: 'sdkwrapper/build/outputs/aar/sdkwrapper-release-*.aar'
					archiveArtifacts artifacts: 'dist/**/SDKWrapper-*-Android.tar.xz'
					archiveArtifacts artifacts: 'build/tar/SDKWrapper-Android-Sources.tgz'
				}
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
