.. _workflow-controller:

Usage
=====

You can use the included WorkflowController to integrate the online identification function into your own UI.

The WorkflowController communicates asynchronously via callbacks, see section :ref:`workflow-controller-callbacks`
for a definition of the callbacks and :ref:`workflow-controller-data-types` for the used data types.
You have to first define and register your own callbacks by calling :ref:`wc-register-callbacks`.
After you have registered the callbacks you can call :ref:`wc-start` to initialize the
WorkflowController.
:ref:`wc-on-started` is called after the AusweisApp SDK Wrapper is initialized, after which you can either start
an authentication (:ref:`wc-start-authentication`) or a PIN change (:ref:`wc-start-change-pin`).
To free up system ressources you can call :ref:`wc-stop` after you are finished using the WorkflowController.

.. include:: workflow-controller-example.rst

.. warning::
  On Android you have to pass through incoming NFC tags to the WorkflowController, as only the active Activity receives them.
  See section :ref:`workflow-controller-nfc-foreground-dispatcher` for more details.

.. toctree::
    :maxdepth: 2

    workflow-controller-api
    workflow-controller-callbacks
    workflow-controller-data-types
    workflow-controller-nfc-foreground-dispatcher
