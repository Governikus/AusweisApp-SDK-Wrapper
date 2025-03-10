The example below shows the minimal worflow to achieve an authentication with a preset PIN.
Empty callback declarations are left out to increase readability.


.. tabs::

    .. code-tab:: kotlin

        import com.governikus.ausweisapp.sdkwrapper.SDKWrapper.workflowController

        internal class WorkflowViewModel(application: Application) : AndroidViewModel(application) {

            [...]

            private val workflowCallbacks = object : WorkflowCallbacks {
               override fun onStarted() {
                   workflowController.startAuthentication(
                       Uri.parse("[...]"),
                       false,
                       false
                   )
               }

               override fun onAuthenticationCompleted(authResult: AuthResult) {
                   val url = authResult?.url ?: return
                   val intent = Intent(Intent.ACTION_VIEW, url)
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                   getApplication<Application>().applicationContext.startActivity(intent)
               }

               override fun onEnterPin(error: String?, reader: Reader) {
                   workflowController.setPin("123456")
               }

               override fun onAccessRights(error: String?, accessRights: AccessRights?) {
                   workflowController.accept()
               }

               [...]
           }

           init {
               workflowController.registerCallbacks(workflowCallbacks)
               workflowController.start(application)
           }

           override fun onCleared() {
               super.onCleared()
               workflowController.unregisterCallbacks(workflowCallbacks)
               workflowController.stop()
           }
        }


    .. code-tab:: swift

        import AusweisApp2SDKWrapper

        @available(iOS 16, *)
        class WorkflowViewModel: ObservableObject {
            private let workflowController = AA2SDKWrapper.workflowController

            init() {
                AA2SDKWrapper.workflowController.registerCallbacks(self)
                AA2SDKWrapper.workflowController.start()
            }

            private func cleanup() {
                AA2SDKWrapper.workflowController.unregisterCallbacks(self)
                if AA2SDKWrapper.workflowController.isStarted {
                    AA2SDKWrapper.workflowController.stop()
                }
            }
        }

        @available(iOS 16, *)
        extension WorkflowViewModel: WorkflowCallbacks {
            func onAccessRights(error: String?, accessRights: AusweisApp2SDKWrapper.AccessRights?) {
                workflowController.accept()
            }

            func onAuthenticationCompleted(authResult: AusweisApp2SDKWrapper.AuthResult) {
                if let url = authResult.url {
                    UIApplication.shared.open(url)
                }
            }

            func onEnterPin(error: String?, reader: AusweisApp2SDKWrapper.Reader) {
                workflowController.setPin("123456")
            }

            func onStarted() {
                let tcTokenUrl = URL(string: "[...]")
                workflowController.startAuthentication(withTcTokenUrl: tcTokenUrl)
            }

            func start() {
                workflowController.registerCallbacks(self)
                workflowController.start()
            }
        }

