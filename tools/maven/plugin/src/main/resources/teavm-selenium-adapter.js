var JUnitClient = {}
JUnitClient.run = function() {
    $rt_startThread(function() {
        var thread = $rt_nativeThread();
        var instance;
        var ptr = 0;
        var message;
        if (thread.isResuming()) {
            ptr = thread.pop();
            instance = thread.pop();
        }
        loop: while (true) { switch (ptr) {
        case 0:
            try {
                runTest();
            } catch (e) {
                message = {};
                JUnitClient.makeErrorMessage(message, e);
                break loop;
            }
            if (thread.isSuspending()) {
                thread.push(instance);
                thread.push(ptr);
                return;
            }
            message = {};
            message.status = "ok";
            break loop;
        }}
        window.parent.postMessage(JSON.stringify(message), "*");
    })
}

JUnitClient.makeErrorMessage = function(message, e) {
    message.status = "exception";
    var stack = e.stack;
    if (e.$javaException && e.$javaException.constructor.$meta) {
        message.exception = e.$javaException.constructor.$meta.name;
        message.stack = e.$javaException.constructor.$meta.name + ": ";
        var exceptionMessage = extractException(e.$javaException);
        message.stack += exceptionMessage ? $rt_ustr(exceptionMessage) : "";
    }
    message.stack += "\n" + stack;
}