package com.trainticket.config;

import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Enumeration;

@Aspect
@Component
public class HttpAspect {

    private final static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HttpAspect.class);


    ThreadLocal<String> parentSpanId = new ThreadLocal<String>();
    ThreadLocal<String> requestId = new ThreadLocal<String>();
    ThreadLocal<String> traceId = new ThreadLocal<String>();
    ThreadLocal<String> spanId = new ThreadLocal<String>();
    ThreadLocal<String> uri = new ThreadLocal<String>();

    ////////////////////////////////// 拦截API调用 //////////////////////////////////////////

    @Pointcut("execution(public * com.trainticket.controller.*.*(..))")
    public void webLog(){
    }

    @Before("webLog()")
    public void doBeforeWeb(JoinPoint joinPoint){
        parentSpanId.set("");
        requestId.set("");
        traceId.set("");
        spanId.set("");
        uri.set("");

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURI();
        uri.set(url);
        String method = request.getMethod();
        String ip = request.getRemoteAddr();
        String remoteHost = request.getRemoteHost();

        StringBuilder sb = new StringBuilder();
        //按固定顺序存储RequestType，RequestId，TraceId，SpanId以及ParentSpanId
        String rt = request.getHeader("request-type") != null ? request.getHeader("request-type") : "";
        String rid = request.getHeader("x-request-id") != null ? request.getHeader("x-request-id") : "";
        String tid = request.getHeader("x-b3-traceid") != null ? request.getHeader("x-b3-traceid") : "";
        String sid = request.getHeader("x-b3-spanid") != null ? request.getHeader("x-b3-spanid") : "";
        String psid = request.getHeader("x-b3-parentspanid") != null ? request.getHeader("x-b3-parentspanid") : "";
        sb.append(String.format("[%s:%s]","request-type",rt));
        sb.append(String.format("[%s:%s]","RequestId",rid));
        requestId.set(rid);
        sb.append(String.format("[%s:%s]","TraceId",tid));
        traceId.set(tid);
        sb.append(String.format("[%s:%s]","SpanId",sid));
        spanId.set(sid);
        sb.append(String.format("[%s:%s]","ParentSpanId",psid));
        parentSpanId.set(psid);

        String requestArgs  = "";
        if(joinPoint.getArgs() != null && joinPoint.getArgs().length > 0){
            for(Object c:  joinPoint.getArgs()) {
                if( !(c instanceof HttpServletResponse) && !(c instanceof HttpServletRequest) &&  !(c instanceof HttpHeaders)) {
                    requestArgs += new Gson().toJson(c);
                }
            }
        }

        logger.info(sb.toString() +
                "[URI:" + url + "]" +
                "[Method:" + method + "]" +
                "[RemoteHost:" + remoteHost + "]" +
                "[IP:" + ip + "]" +
                "[LogType:InvocationRequest]" +
                "[Request:" + requestArgs + "]" );
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturningWeb(Object ret) throws Throwable {

        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();

        String traceInfo = "[ParentSpanId:" + parentSpanId.get() + "]" +
                "[RequestId:" + requestId.get() + "]" +
                "[TraceId:" + traceId.get() + "]" +
                "[SpanId:" + spanId.get() + "]" +
                "[LogType:InvocationResponse]" +
                "[URI:" + uri.get() + "]" +
                "[ResponseCode:" + response.getStatus() + "]" +
                "[CodeMessage:" + HttpStatus.valueOf(response.getStatus()).getReasonPhrase() + "]";

        if(ret != null){
            logger.info(traceInfo + "[Response:" + new Gson().toJson(ret) + "]");
        }else{
            logger.info(traceInfo + "[Response:void]");
        }
    }

    /////////////////////////////////// 打印异常日志 //////////////////////////////////////////////

    @AfterThrowing(value = "execution(public * com.trainticket.controller.*.*(..))", throwing = "e")
    private void doAfterThrow(Throwable e) {

        logger.info("[ParentSpanId:" + parentSpanId.get() + "]" +
                "[RequestId:" + requestId.get() + "]" +
                "[TraceId:" + traceId.get() + "]" +
                "[SpanId:" + spanId.get() + "]" +
                "[LogType:InternalMethod]" +
                "[URI:" + uri.get() + "]" +
                "[ExceptionMessage:" + e.toString() + "]" +
                "[ExceptionCause:" + e.getCause() + "]" +
                "[ExceptionStack:" + Arrays.toString(e.getStackTrace()) + "]");

    }

    ///////////////////////////////////// 拦截方法内部日志 //////////////////////////////////////////////////

    @Pointcut("execution(public * com.trainticket.config.MockLog.printLog(..))")
    public void methodLog(){
    }

    @Before("methodLog()")
    public void doBeforeMethod(JoinPoint joinPoint){
        String content = "";
        if(joinPoint.getArgs() != null && joinPoint.getArgs().length == 1){
            content = (String)joinPoint.getArgs()[0];
        }
        logger.info("[ParentSpanId:" + parentSpanId.get() + "]" +
                "[RequestId:" + requestId.get() + "]" +
                "[TraceId:" + traceId.get() + "]" +
                "[SpanId:" + spanId.get() + "]" +
                "[LogType:InternalMethod]" +
                "[URI:" + uri.get() + "]" +
                "[Content:" + content + "]");
    }


}