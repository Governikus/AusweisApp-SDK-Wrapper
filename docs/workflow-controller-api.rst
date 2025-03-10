.. _workflow-controller-api:

WorkflowController API
^^^^^^^^^^^^^^^^^^^^^^

This section describes the API of the WorkflowController class.
You should not create your own instance of the WorkflowController but use the static member of the
SDKWrapper class instead.

.. tabs::

  .. code-tab:: kotlin

    import com.governikus.ausweisapp.sdkwrapper.SDKWrapper.workflowController

  .. code-tab:: swift

    import AusweisApp2SDKWrapper

    let workflowController = AA2SDKWrapper.workflowController

.. _wc-is-started:

isStarted
---------

Indicates that the WorkflowController is ready to be used.
When the WorkflowController is not in started state other API calls will fail.

.. tabs::

  .. code-tab:: kotlin

    val isStarted: Boolean

  .. code-tab:: swift

    public var isStarted: Bool

.. _wc-start:

start
-----

Initialize the WorkflowController.

Before it is possible to use the WorkflowController it needs to be initialized.
Make sure to call this function and wait for the :ref:`wc-on-started` callback before using it.

.. note::
  *Android only*.

  - **context**:
    Android Context in which the WorkflowController lives.

.. tabs::

  .. code-tab:: kotlin

    fun start(context: Context)

  .. code-tab:: swift

    public func start()

.. _wc-stop:

stop
----

Stop the WorkflowController.

When you no longer need the WorkflowController make sure to stop it to free up some resources.

.. tabs::

  .. code-tab:: kotlin

    fun stop()

  .. code-tab:: swift

    public func stop()

.. _wc-register-callbacks:

registerCallbacks
-----------------

Register callbacks with controller.
See :ref:`workflow-controller-callbacks` for more information about callbacks.

- **callbacks**:
  Callbacks to register.

.. tabs::

  .. code-tab:: kotlin

    fun registerCallbacks(callbacks: WorkflowCallbacks)

  .. code-tab:: swift

    public func registerCallbacks(_ callbacks: WorkflowCallbacks)

.. _wc-unregister-callbacks:

unregisterCallbacks
-------------------

Unregister callback from controller.

- **callbacks**:
  Callbacks to unregister.

.. tabs::

  .. code-tab:: kotlin

    fun unregisterCallbacks(callbacks: WorkflowCallbacks)

  .. code-tab:: swift

    public func unregisterCallbacks(_ callbacks: WorkflowCallbacks)

.. _wc-start-authentication:

startAuthentication
-------------------

Starts an authentication workflow.
This implements the command :external:ref:`run_auth` of the AusweisApp SDK.

The WorkflowController will call :ref:`wc-on-authentication-started`,
when the authentication is started. If the authentication could not be started,
you will get a callback to :ref:`wc-on-authentication-start-failed`.

After calling this method, the expected minimal workflow is:

1. :ref:`wc-on-authentication-started` is called.
2. When :ref:`wc-on-access-rights` is called, accept it via :ref:`wc-accept`.
3. :ref:`wc-on-insert-card` is called, when the user has not yet placed the phone on the card.
4. When :ref:`wc-on-enter-pin` is called, provide the PIN via :ref:`wc-set-pin`.
5. When the authentication workflow is finished :ref:`wc-on-authentication-completed` is called.

This command is allowed only if the SDK has no running workflow.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **tcTokenURL**:
  URL to the TcToken of the eID service provider you want to authenticate against.

- **developerMode**:
  Enable "Developer Mode" for test cards and disable some security checks according to BSI TR-03124-1.

- **status**:
  True to enable automatic STATUS messages, which are delivered by callbacks to
  :ref:`wc-on-status`.

.. note::
  *iOS only*.

  - **userInfoMessages**:
    Messages for the NFC system dialog.
    See :ref:`wc-userinfomessages`.

.. tabs::

  .. code-tab:: kotlin

    fun startAuthentication(
      tcTokenUrl: Uri,
      developerMode: Boolean = false,
      status: Boolean = true)

  .. code-tab:: swift

    public func startAuthentication(
      withTcTokenUrl tcTokenUrl: URL,
      withDeveloperMode developerMode: Bool = false,
      withUserInfoMessages userInfoMessages: AA2UserInfoMessages? = nil,
      withStatusMsgEnabled status: Bool = true)

.. _wc-start-change-pin:

startChangePin
--------------

Start a PIN change workflow.
This implements the command :external:ref:`run_change_pin` of the AusweisApp SDK.

The WorkflowController will call :ref:`wc-on-change-pin-started`,
when the PIN change is started.

After calling this method, the expected minimal workflow is:

1. :ref:`wc-on-change-pin-started` is called.
2. :ref:`wc-on-insert-card` is called, when the user has not yet placed the card on the reader.
3. When :ref:`wc-on-enter-pin` is called, provide the PIN via :ref:`wc-set-pin`.
4. When :ref:`wc-on-enter-new-pin` is called, provide the new PIN via :ref:`wc-set-new-pin`.
5. When the PIN workflow is finished, :ref:`wc-on-change-pin-completed` is called.

This command is allowed only if the SDK has no running workflow.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **status**:
  True to enable automatic STATUS messages, which are delivered by callbacks to
  :ref:`wc-on-status`.

.. note::
  *iOS only*.

  - **userInfoMessages**:
    Messages for the NFC system dialog.
    See :ref:`wc-userinfomessages`.

.. tabs::

  .. code-tab:: kotlin

    fun startChangePin(status: Boolean = true)

  .. code-tab:: swift

    public func startChangePin(
      withUserInfoMessages userInfoMessages: AA2UserInfoMessages? = nil,
      withStatusMsgEnabled status: Bool = true)

.. _wc-set-access-rights:

setAccessRights
---------------

Set optional access rights.
This implements the command :external:ref:`set_access_rights` of the AusweisApp SDK.

If the SDK Wrapper asks for specific access rights in :ref:`wc-on-access-rights`,
you may modify the requested optional rights by setting a list of accepted optional rights here.
When the command is successful you get a callback to :ref:`wc-on-access-rights`
with the updated access rights.

List of possible access rights are listed in :ref:`wc-access-right`

This command is allowed only if the SDK Wrapper asked for access rights via :ref:`wc-on-access-rights`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **optionalAccessRights**:
  List of enabled optional access rights.
  If the list is empty all optional access rights are disabled.
  See section :ref:`wc-access-right`.

.. tabs::

  .. code-tab:: kotlin

    fun setAccessRights(optionalAccessRights: List<AccessRight>)

  .. code-tab:: swift

    public func setAccessRights(_ optionalAccessRights: [AccessRight])

.. _wc-get-access-rights:

getAccessRights
---------------

Returns information about the requested access rights.

This implements the command :external:ref:`get_access_rights` of the AusweisApp SDK.

.. note::
  This command is allowed only if the SDK Wrapper called :ref:`wc-on-access-rights` beforehand.

.. tabs::

  .. code-tab:: kotlin

    fun getAccessRights()

  .. code-tab:: swift

    public func getAccessRights()

.. _wc-interrupt:

interrupt
---------

Allows to interrupt the iOS NFC Dialog.
This implements the command :external:ref:`interrupt` of the AusweisApp SDK.

Your application may want to suppress the iOS NFC Dialog to display some UI elements to the user.
The command is only permitted after being asked for PIN/CAN/PUK. The SDK will restart the iOS NFC scan as needed.

.. tabs::


  .. code-tab:: swift

    public func interrupt()

.. _wc-set-pin:

setPin
------

Set PIN of inserted card.
This implements the command :external:ref:`set_pin` of the AusweisApp SDK.

If the SDK Wrapper calls :ref:`wc-on-enter-pin` you need to allow access to the card with the given PIN.

If your application provides an invalid PIN the SDK Wrapper will call :ref:`wc-on-enter-pin`
again with a decreased :ref:`wc-card`.retryCounter.

If the value of :ref:`wc-card`.retryCounter is 1 the SDK will initially call :ref:`wc-on-enter-can`.
Once your application provides a correct CAN the SDK Wrapper will call :ref:`wc-on-enter-pin`
again with a :ref:`wc-card`.retryCounter of 1.
If the value of :ref:`wc-card`.retryCounter is 0 the SDK will initially call :ref:`wc-on-enter-puk`.
Once your application provides a correct PUK the SDK Wrapper will call :ref:`wc-on-enter-pin`
again with a :ref:`wc-card`.retryCounter of 3.

This command is allowed only if the SDK Wrapper asked for a PIN via :ref:`wc-on-enter-pin`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **pin**:
  The Personal Identification Number (PIN) of the card.
  This must be 6 digits in an :ref:`authentication workflow<wc-start-authentication>`. If a
  :ref:`pin change workflow<wc-start-change-pin>` is in progress the value must
  be 5 or 6 digits because of a possible transport PIN.
  Must be nil if the current reader has a keypad. See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun setPin(pin: String?)

  .. code-tab:: swift

    public func setPin(_ pin: String?)

.. _wc-set-new-pin:

setNewPin
---------

Set new PIN for inserted card.
This implements the command :external:ref:`set_new_pin` of the AusweisApp SDK.

If the SDK Wrapper calls :ref:`wc-on-enter-new-pin` you need to call this function to provide a new pin.

This command is allowed only if the SDK Wrapper asked for a new PIN via :ref:`wc-on-enter-new-pin`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **newPin**:
  The new personal identification number (PIN) of the card.
  Must only contain 6 digits.
  Must be nil if the current reader has a keypad. See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun setNewPin(newPin: String?)

  .. code-tab:: swift

    public func setNewPin(_ newPin: String?)

.. _wc-set-puk:

setPuk
------

Set PUK of inserted card.
This implements the command :external:ref:`set_puk` of the AusweisApp SDK.

If the SDK Wrapper calls :ref:`wc-on-enter-puk` you need to call this function to unblock :ref:`wc-set-pin`.

The workflow will automatically continue if the PUK was correct and the SDK Wrapper will call :ref:`wc-on-enter-pin`.
If the correct PUK is entered the retryCounter will be set to 3.

If your application provides an invalid PUK the SDK Wrapper will call :ref:`wc-on-enter-puk` again.

If the SDK Wrapper calls :ref:`wc-on-enter-puk` with :ref:`wc-card`.inoperative set true it is not possible to unblock
the PIN.
You will have to show a message to the user that the card is inoperative and the user should
contact the authority responsible for issuing the identification card to unblock the PIN.

This command is allowed only if the SDK Wrapper asked for a PUK via :ref:`wc-on-enter-puk`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **puk**:
  The personal unblocking key (PUK) of the card.
  Must only contain 10 digits.
  Must be nil if the current reader has a keypad. See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun setPuk(puk: String?)

  .. code-tab:: swift

    public func setPuk(_ puk: String?)

.. _wc-set-can:

setCan
------

Set CAN of inserted card.
This implements the command :external:ref:`set_can` of the AusweisApp SDK.

If the SDK Wrapper calls :ref:`wc-on-enter-can` you need to call this function
to unblock the last retry of :ref:`wc-set-pin`.

The CAN is required to enable the last attempt of PIN input if the retryCounter is 1.
The workflow continues automatically with the correct CAN and the SDK Wrapper will call :ref:`wc-on-enter-pin`.
Despite the correct CAN being entered, the retryCounter remains at 1.
The CAN is also required, if the authentication terminal has an approved *CAN-allowed right*.
This allows the workflow to continue without an additional PIN.

If your application provides an invalid CAN the SDK Wrapper will call :ref:`wc-on-enter-can` again.

This command is allowed only if the SDK Wrapper asked for a CAN via :ref:`wc-on-enter-can`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

- **can**:
  The card access number (CAN) of the card.
  Must only contain 6 digits.
  Must be nil if the current reader has a keypad. See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun setCan(can: String?)

  .. code-tab:: swift

    public func setCan(_ can: String?)

.. _wc-accept:

accept
------

Accept the current state.
This implements the command :external:ref:`accept` of the AusweisApp SDK.

If the SDK Wrapper calls :ref:`wc-on-access-rights` the user needs to accept or deny them.
The workflow is paused until your application sends this command to accept the requested information.
If the user does not accept the requested information your application needs to call :ref:`wc-cancel` to abort the
whole workflow.

This command is allowed only if the SDK Wrapper asked for access rights via :ref:`wc-on-access-rights`.
Otherwise you will get a callback to :ref:`wc-on-bad-state`.

Note: This accepts the requested access rights as well as the provider's certificate since it is not possible to
accept one without the other.

.. tabs::

  .. code-tab:: kotlin

    fun accept()

  .. code-tab:: swift

    public func accept()

.. _wc-cancel:

cancel
------

Cancel the running workflow.
This implements the command :external:ref:`cancel` of the AusweisApp SDK.

If your application sends this command the SDK will cancel the workflow.
You may send this command in any state of a running workflow to abort it.

.. tabs::

  .. code-tab:: kotlin

    fun cancel()

  .. code-tab:: swift

    public func cancel()

.. _wc-continue-workflow:

continueWorkflow
----------------

Resumes the workflow after a callback to :ref:`wc-on-pause`.
This implements the command :external:ref:`continue_cmd` of the AusweisApp SDK.

.. tabs::

  .. code-tab:: kotlin

    fun continueWorkflow()

  .. code-tab:: swift

    public func continueWorkflow()

.. _wc-get-certificate:

getCertificate
--------------

Request the certificate of current authentication.
This implements the command :external:ref:`get_certificate` of the AusweisApp SDK.

The SDK Wrapper will call :ref:`wc-on-certificate` as an answer.

.. tabs::

  .. code-tab:: kotlin

    fun getCertificate()

  .. code-tab:: swift

    public func getCertificate()

.. _wc-set-card:

setCard
-------

Insert "virtual" card.

- **name**:
  Name of reader with a Card that shall be used.

- **simulator**:
  Optional specific Filesystem data for Simulator reader.
  See :ref:`wc-simulator`.

.. tabs::

  .. code-tab:: kotlin

    fun setCard(name: String, simulator: Simulator? = nil)

  .. code-tab:: swift

    public func setCard(name: String, simulator: Simulator? = nil)

.. _wc-get-status:

getStatus
---------

Request information about the current workflow and state of SDK.
This implements the command :external:ref:`get_status` of the AusweisApp SDK.

The SDK Wrapper will call :ref:`wc-on-status` as an answer.

.. tabs::

  .. code-tab:: kotlin

    fun getStatus()

  .. code-tab:: swift

    public func getStatus()

.. _wc-get-info:

getInfo
-------

Provides information about the employed AusweisApp.
This implements the command :external:ref:`get_info` of the AusweisApp SDK.

The SDK Wrapper will call WorkflowCallbacks.onInfo() as an answer.

.. tabs::

  .. code-tab:: kotlin

    fun getInfo()

  .. code-tab:: swift

    public func getInfo()

.. _wc-get-reader:

getReader
---------

Returns information about the requested reader.
This implements the command :external:ref:`get_reader` of the AusweisApp SDK.

If you explicitly want to ask for information of a known reader name you can request it with this command.

The SDK Wrapper will call :ref:`wc-on-reader` as an answer.

- **name**:
  Name of the reader.

.. tabs::

  .. code-tab:: kotlin

    fun getReader(name: String)

  .. code-tab:: swift

    public func getReader(name: String)

.. _wc-get-reader-list:

getReaderList
-------------

Returns information about all connected readers.
This implements the command :external:ref:`get_reader_list` of the AusweisApp SDK.

If you explicitly want to ask for information of all connected readers you can request it with this command.

The SDK Wrapper will call :ref:`wc-on-reader-list` as an answer.

.. tabs::

  .. code-tab:: kotlin

    fun getReaderList()

  .. code-tab:: swift

    public func getReaderList()

.. _wc-on-nfc-tag-detected:

onNfcTagDetected
----------------

**Android only**

Pass a detected NFC tag to the WorkflowController

Since only a foreground application can detect NFC tags,
you need to pass them to the SDK for it to handle detected ID cards.

See :ref:`workflow-controller-nfc-foreground-dispatcher`

- **tag**:
  Detected ID card. ISO-DEP (ISO 14443-4) NFC tag

.. code-block:: kotlin

  fun onNfcTagDetected(tag: Tag)
