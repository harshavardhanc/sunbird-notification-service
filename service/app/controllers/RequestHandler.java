package controllers;

import akka.pattern.Patterns;
import akka.util.Timeout;
import org.sunbird.BaseException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Result;
import play.mvc.Results;
import scala.concurrent.Await;
import scala.concurrent.Future;
import utils.JsonKey;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;


/**
 * this class is used to handle the request and ask from actor and return response on the basis of success and failure to user.
 * @author amitkumar
 */
public class RequestHandler extends BaseController {/**
     * this methis responsible to handle the request and ask from actor
     * @param request
     * @param httpExecutionContext
     * @param operation
     * @return CompletionStage<Result>
     * @throws Exception
     */
    public CompletionStage<Result> handleRequest(Request request, HttpExecutionContext httpExecutionContext, String operation) throws Exception {
        Object obj;
        CompletableFuture<String> cf = new CompletableFuture<>();
        request.setOperation(operation);
        //ProjectLogger.log(String.format("%s:%s:Requested operation %s",this.getClass().getSimpleName(),"handleRequest",operation), LoggerEnum.DEBUG.name());
        //startTrace("handleRequest");
        Timeout t = new Timeout(Long.valueOf(request.getTimeout()), TimeUnit.SECONDS);
        Future<Object> future = Patterns.ask(getActorRef(operation), request, t);
        obj = Await.result(future, t.duration());
        //endTrace("handleRequest");
        return handleResponse(obj,httpExecutionContext);
    }

    /**
     * This method will handle all the failure response of Api calls.
     *
     * @param exception
     * @return
     */
    public static CompletionStage<Result> handleFailureResponse(Object exception, HttpExecutionContext httpExecutionContext) {

        Response response = new Response();
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        if (exception instanceof BaseException) {
            BaseException ex = (BaseException) exception;
            response.setResponseCode(ResponseCode.BAD_REQUEST);
            response.put(JsonKey.MESSAGE, ex.getMessage());
            future.complete(Json.toJson(response));
            if (ex.getResponseCode() == Results.badRequest().status()) {
                return future.thenApplyAsync(Results::badRequest, httpExecutionContext.current());
            } else {
                return future.thenApplyAsync(Results::internalServerError, httpExecutionContext.current());
            }
        } else {
            response.setResponseCode(ResponseCode.SERVER_ERROR);
            response.put(JsonKey.MESSAGE,localizerObject.getMessage(IResponseMessage.INTERNAL_ERROR,null));
            future.complete(Json.toJson(response));
            return future.thenApplyAsync(Results::internalServerError, httpExecutionContext.current());
        }
    }

    /**
     * this method will divert the response on the basis of success and failure
     * @param object
     * @param httpExecutionContext
     * @return
     */
    public  static CompletionStage<Result> handleResponse(Object object, HttpExecutionContext httpExecutionContext) {

        if (object instanceof Response) {
            Response response = (Response) object;
            return handleSuccessResponse(response, httpExecutionContext);
        } else {
            return handleFailureResponse(object, httpExecutionContext);
        }
    }

    /**
     * This method will handle all the success response of Api calls.
     *
     * @param response
     * @return
     */

    public static CompletionStage<Result> handleSuccessResponse(Response response, HttpExecutionContext httpExecutionContext) {
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        future.complete(Json.toJson(response));
        return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
    }
    }
