package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;

public abstract class SpanAsyncEventSimpleAroundInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected static final String SCOPE_NAME = "##ASYNC_TRACE_SCOPE";

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;
    final MethodDescriptor asyncMethodDescriptor = new AsyncMethodDescriptor();

    public SpanAsyncEventSimpleAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (methodDescriptor == null) {
            throw new NullPointerException("methodDescriptor must not be null");
        }

        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;

        traceContext.cacheApi(asyncMethodDescriptor);
    }


    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final AsyncTraceId asyncTraceId = getAsyncTraceId(target);
        if (asyncTraceId == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            // create async trace;
            trace = createAsyncTrace(asyncTraceId);
            if (trace == null) {
                return;
            }
        } else {
            // check sampled.
            if (!trace.canSampled()) {
                // sckip.
                return;
            }
        }

        // entry scope.
        entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, asyncTraceId, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected abstract void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (getAsyncTraceId(target) == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope.
        if (!leaveAsyncTraceScope(trace)) {
            logger.warn("Failed to leave scope of async trace {}.", trace);
            // delete unstable trace.
            deleteAsyncTrace(trace);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (isAsyncTraceDestination(trace)) {
                deleteAsyncTrace(trace);
            }
        }
    }

    protected abstract void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable);

    private AsyncTraceId getAsyncTraceId(Object target) {
        return target != null && target instanceof AsyncTraceIdAccessor ? ((AsyncTraceIdAccessor) target)._$PINPOINT$_getAsyncTraceId() : null;
    }

    private Trace createAsyncTrace(AsyncTraceId asyncTraceId) {
        final Trace trace = traceContext.continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
        if (trace == null) {
            logger.warn("Failed to continue async trace. 'result is null'");
            return null;
        }
        if (isDebug) {
            logger.debug("Continue async trace {}, id={}", trace, asyncTraceId);
        }

        // add async scope.
        TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            logger.warn("Duplicated async trace scope={}.", oldScope.getName());
            // delete corrupted trace.
            deleteAsyncTrace(trace);
            return null;
        }

        // first block.
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        recorder.recordServiceType(ServiceType.ASYNC);
        recorder.recordApi(asyncMethodDescriptor);

        return trace;
    }

    private void deleteAsyncTrace(final Trace trace) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }
        traceContext.removeTraceObject();
        trace.close();
    }

    private void entryAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
        }
    }

    private boolean leaveAsyncTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isAsyncTraceDestination(final Trace trace) {
        if (!trace.isAsync()) {
            return false;
        }

        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null && !scope.isActive();
    }

    public class AsyncMethodDescriptor implements MethodDescriptor {

        private int apiId = 0;

        @Override
        public String getMethodName() {
            return "";
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public String[] getParameterTypes() {
            return null;
        }

        @Override
        public String[] getParameterVariableName() {
            return null;
        }

        @Override
        public String getParameterDescriptor() {
            return "";
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public String getFullName() {
            return AsyncMethodDescriptor.class.getName();
        }

        @Override
        public void setApiId(int apiId) {
            this.apiId = apiId;
        }

        @Override
        public int getApiId() {
            return apiId;
        }

        @Override
        public String getApiDescriptor() {
            return "Asynchronous Invocation";
        }

        @Override
        public int getType() {
            return 200;
        }
    }
}