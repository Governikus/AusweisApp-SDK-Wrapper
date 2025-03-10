.. _workflow-controller-callbacks:

WorkflowController callbacks
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. _wc-on-started:

onStarted
---------

WorkflowController has successfully been initialized.

.. tabs::

  .. code-tab:: kotlin

    fun onStarted()

  .. code-tab:: swift

    func onStarted()

.. _wc-on-authentication-started:

onAuthenticationStarted
-----------------------

An authentication has been started via :ref:`wc-start-authentication`.
This callback handles the message :external:ref:`auth` of the AusweisApp SDK.

The next callback should be :ref:`wc-on-access-rights` or :ref:`wc-on-authentication-completed` if the
authentication immediately results in an error.

.. tabs::

  .. code-tab:: kotlin

    fun onAuthenticationStarted()

  .. code-tab:: swift

    func onAuthenticationStarted()

.. _wc-on-authentication-start-failed:

onAuthenticationStartFailed
---------------------------

An authentication could not be started.
This is different from an authentication that was started but failed during the process.

- **error**:
  Error message about why the authentication could not be started.

.. tabs::

  .. code-tab:: kotlin

    fun onAuthenticationStartFailed(error: String)

  .. code-tab:: swift

    func onAuthenticationStartFailed(error: String)

.. _wc-on-change-pin-started:

onChangePinStarted
------------------

A PIN change has been started via :ref:`wc-start-change-pin`.
This callback handles the message :external:ref:`change_pin` of the AusweisApp SDK.

.. tabs::

  .. code-tab:: kotlin

    fun onChangePinStarted()

  .. code-tab:: swift

    func onChangePinStarted()

.. _wc-on-access-rights:

onAccessRights
--------------

Access rights requested in response to an authentication.
This callback handles the message :external:ref:`access_rights` of the AusweisApp SDK.

This function will be called once the authentication is started by :ref:`wc-start-authentication`
and the SDK got the certificate from the service provider.

Accept (:ref:`wc-accept`) the rights to continue with the workflow or completely
abort the workflow with (:ref:`wc-cancel`).

It is also possible to change the optional rights via :ref:`wc-set-access-rights`.

- **error**:
  Optional error message if the call to :ref:`wc-set-access-rights` failed.

- **accessRights**:
  Requested access rights.
  See section :ref:`wc-access-rights`.

.. tabs::

  .. code-tab:: kotlin

    fun onAccessRights(error: String?, accessRights: AccessRights?)

  .. code-tab:: swift

    func onAccessRights(error: String?, accessRights: AccessRights?)

.. _wc-on-certificate:

onCertificate
-------------

Provides information about the used certificate.
This callback handles the message :external:ref:`certificate` of the AusweisApp SDK.

Response to a call to :ref:`wc-get-certificate`.

- **certificateDescription**:
  Requested certificate.
  See section :ref:`wc-certificate-description`.

.. tabs::

  .. code-tab:: kotlin

    fun onCertificate(certificateDescription: CertificateDescription)

  .. code-tab:: swift

    func onCertificate(certificateDescription: CertificateDescription)

.. _wc-on-insert-card:

onInsertCard
------------

Indicates that the workflow now requires an ID card to continue.
This callback handles the message :external:ref:`insert_card` of the AusweisApp SDK.
Also called as a response to :ref:`wc-get-reader`.

If your application receives this message it should show a hint to the user.
After the user inserted a card the workflow will automatically continue, unless the eID functionality is disabled.
In this case, the workflow will be paused until another card is inserted.

.. note::
  *iOS only*

  iOS shows its own NFC reading system dialog while the AusweisApp SDK Wrapper is expecting an ID card.

.. warning::
  *Android only*

  This callback may not be called on Android if the user already inserted a card.

- **error**: Optional detailed error message if the previous call to :ref:`wc-set-card` failed.

.. tabs::

  .. code-tab:: kotlin

    fun onInsertCard(error: String?)

  .. code-tab:: swift

    func onInsertCard(error: String?)

.. _wc-on-pause:

onPause
-------

Called if the SDK is waiting on a certain condition to be met.
This callback handles the message :external:ref:`pause_message` of the AusweisApp SDK.

After resolving the cause of the issue, the workflow has to be resumed by calling
:ref:`wc-continue-workflow`.

- **cause**: The cause for the waiting condition.
  See :ref:`wc-cause`.

.. tabs::

  .. code-tab:: kotlin

    fun onPause(cause: Cause)

  .. code-tab:: swift

    func onPause(cause: Cause)

.. _wc-on-reader:

onReader
--------

A specific reader was recognized or has vanished.
Also called as a response to :ref:`wc-get-reader`.
This callback handles the message :external:ref:`reader` of the AusweisApp SDK.

- **reader**:
  Recognized or vanished reader, might be nil if an unknown reader was requested in :ref:`wc-get-reader`.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onReader(reader: Reader?)

  .. code-tab:: swift

    func onReader(reader: Reader?)

.. _wc-on-reader-list:

onReaderList
------------

Called as a reponse to :ref:`wc-get-reader-list`.
This callback handles the message :external:ref:`reader_list` of the AusweisApp SDK.

- **readers**:
  Optional list of present readers.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onReaderList(reader: List<Reader>?)

  .. code-tab:: swift

    func onReaderList(readers: [Reader]?)

.. _wc-on-enter-pin:

onEnterPin
----------

Indicates that a PIN is required to continue the workflow.
This callback handles the message :external:ref:`enter_pin` of the AusweisApp SDK.

A PIN is needed to unlock the ID card, provide it with :ref:`wc-set-pin`.
See section :ref:`wc-card`.

- **error**:
  Optional error message if the last call to :ref:`wc-set-pin` failed.

- **reader**:
  Information about the used card and card reader.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onEnterPin(error: String?, reader: Reader)

  .. code-tab:: swift

    func onEnterPin(error: String?, reader: Reader)

.. _wc-on-enter-new-pin:

onEnterNewPin
-------------

Indicates that a new PIN is required to continue the workflow.
This callback handles the message :external:ref:`enter_new_pin` of the AusweisApp SDK.

A new PIN is needed in response to a PIN change, provide it with :ref:`wc-set-new-pin`.

- **error**:
  Optional error message if the last call to :ref:`wc-set-new-pin` failed.

- **reader**:
  Information about the used card and card reader.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onEnterNewPin(error: String?, reader: Reader)

  .. code-tab:: swift

    func onEnterNewPin(error: String?, reader: Reader)

.. _wc-on-enter-puk:

onEnterPuk
----------

Indicates that a PUK is required to continue the workflow.
This callback handles the message :external:ref:`enter_puk` of the AusweisApp SDK.

A PUK is needed to unlock the ID card, provide it with :ref:`wc-set-puk`.
See section :ref:`wc-card`.

- **error**:
  Optional error message if the last call to :ref:`wc-set-puk` failed.

- **reader**:
  Information about the used card and card reader.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onEnterPuk(error: String?, reader: Reader)

  .. code-tab:: swift

    func onEnterPuk(error: String?, reader: Reader)

.. _wc-on-enter-can:

onEnterCan
----------

Indicates that a CAN is required to continue workflow.
This callback handles the message :external:ref:`enter_can` of the AusweisApp SDK.

A CAN is needed to unlock either the ID card or the third PIN attempt,
provide it with :ref:`wc-set-can`.

- **error**:
  Optional error message if the last call to :ref:`wc-set-can` failed.

- **reader**:
  Information about the used card and card reader.
  See :ref:`wc-reader`.

.. tabs::

  .. code-tab:: kotlin

    fun onEnterCan(error: String?, reader: Reader)

  .. code-tab:: swift

    func onEnterCan(error: String?, reader: Reader)

.. _wc-on-authentication-completed:

onAuthenticationCompleted
-------------------------

Indicates that the authentication workflow is completed.
This callback handles the message :external:ref:`auth` of the AusweisApp SDK.

The authResult will contain a refresh url or in case of an error a communication error address.
You can check the state of the authentication, by looking for the :ref:`wc-auth-result`.error field, null on success.

- **authResult**:
  Result of the authentication.
  See section :ref:`wc-auth-result`.

.. tabs::

  .. code-tab:: kotlin

    fun onAuthenticationCompleted(authResult: AuthResult)

  .. code-tab:: swift

    func onAuthenticationCompleted(authResult: AuthResult)

.. _wc-on-change-pin-completed:

onChangePinCompleted
--------------------

Indicates that the PIN change workflow is completed.
This callback handles the message :external:ref:`change_pin` of the AusweisApp SDK.

- **changePinResult**:
  Result of the PIN change.
  See section :ref:`wc-change-pin-result`.

.. tabs::

  .. code-tab:: kotlin

    fun onChangePinCompleted(changePinResult: ChangePinResult)

  .. code-tab:: swift

    func onChangePinCompleted(changePinResult: ChangePinResult)

.. _wc-on-status:

onStatus
--------

Provides information about the current workflow and state. This callback indicates if a
workflow is in progress or the workflow is paused. This can occur if the AusweisApp needs
additional data like ACCESS_RIGHTS or INSERT_CARD.
This callback handles the message :external:ref:`status` of the AusweisApp SDK.

- **workflowProgress**:
  Holds information about the current workflow progress.
  See section :ref:`wc-workflowprogress`.

.. tabs::

  .. code-tab:: kotlin

    fun onStatus(workflowProgress: WorkflowProgress)

  .. code-tab:: swift

    func onStatus(workflowProgress: WorkflowProgress)

.. _wc-on-info:

onInfo
------

Provides information about the AusweisApp that is used in the SDK Wrapper.
This callback handles the message :external:ref:`info` of the AusweisApp SDK.

Response to a call to :ref:`wc-get-info`.

- **versionInfo**:
  Holds information about the currently utilized AusweisApp.
  See :ref:`wc-versioninfo`.

.. tabs::

  .. code-tab:: kotlin

    fun onInfo(versionInfo: VersionInfo)

  .. code-tab:: swift

    func onInfo(versionInfo: VersionInfo)

.. _wc-on-wrapper-error:

onWrapperError
--------------

Indicates that an error within the SDK Wrapper has occurred.

This might be called if there was an error in the workflow.

- **error**:
  Contains information about the error.
  See :ref:`wc-wrapper-error`.

.. tabs::

  .. code-tab:: kotlin

    fun onWrapperError(error: WrapperError)

  .. code-tab:: swift

    func onWrapperError(error: WrapperError)

.. _wc-on-bad-state:

onBadState
----------

Called if the sent command is not allowed within the current workflow.
This callback handles the message :external:ref:`bad_state` of the AusweisApp SDK.

- **error**:
  Error message which SDK command failed.
  See :external:doc:`messages`.

.. tabs::

  .. code-tab:: kotlin

    fun onBadState(error: String)

  .. code-tab:: swift

    func onBadState(error: String)

.. _wc-on-internal-error:

onInternalError
---------------

Called if an error within the AusweisApp SDK occurred. Please report this as it indicates a bug.
This callback handles the message :external:ref:`internal_error` of the AusweisApp SDK.

- **error**:
  Information about the error.

.. tabs::

  .. code-tab:: kotlin

    fun onInternalError(error: String)

  .. code-tab:: swift

    func onInternalError(error: String)
