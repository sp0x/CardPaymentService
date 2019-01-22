package bg.icafe.network.http;

import bg.icafe.Config;
import bg.icafe.network.mq.TransactionClient;
import bg.icafe.payment.ECOMMHelper;
import bg.icafe.payment.TransactionRedirections;
import bg.icafe.payment.TransactionResult;
import lv.tietoenator.cs.ecomm.merchant.Merchant;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import static bg.icafe.Config.getMerchantConfiguration;

public class TransactionCallbackServlet extends HttpServlet {

    private Merchant _merchant;
    private Properties _props;

    public TransactionCallbackServlet(){

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String spath = req.getServletPath();
        if(spath.equals("/ok")){
            try {
                handleSuccessfullTransaction(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(spath.equals("/failed")){
            try {
                handleFailedTransaction(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles a request saying the transaction failed.
     * @param req
     * @param resp
     * @throws Exception
     */
    private void handleFailedTransaction(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        OutputStream outStream = resp.getOutputStream();
        PrintWriter writer = new PrintWriter(outStream);

        String transactionId = req.getParameter("trans_id");
        TransactionClient tc = TransactionClient.getInstance();
        TransactionRedirections redirections = tc.getRedirectionsForTransaction(transactionId);
        ECOMMHelper ec = tc.getECOMM();

        TransactionResult tres = ec.getTransactionStatus(transactionId, true);
        tc.reportFailedTransaction(transactionId, "Failed", tres);
        onTransactionFailed(resp, writer, redirections, tres);
    }

    /**
     * Handles the failure of a transaction and what happens with the web request.
     * @param resp
     * @param writer
     * @param redirections
     * @param tres
     * @throws IOException
     */
    private void onTransactionFailed(HttpServletResponse resp, PrintWriter writer, TransactionRedirections redirections, TransactionResult tres) throws IOException {
        if(redirections.hasFailed()){
            resp.sendRedirect(resp.encodeRedirectURL(redirections.getOnFailed()));
        }else{
            writer.println("Transaction failed");
            writer.print("Reason: ");
            switch(tres.getResult()){
                case Declined:
                    writer.println("Declined");
                    break;
                case Failed:
                    writer.println("Failed");
                    break;
                case Timeout:
                    writer.println("Timeout");
                    break;
                default:
                    writer.println("Unknown");
                    break;
            }
        }
    }

    /**
     * Handles a request saying the transaction went ok or there as an error.
     * @param req
     * @param resp
     * @throws Exception
     */
    private void handleSuccessfullTransaction(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        OutputStream outStream = resp.getOutputStream();
        PrintWriter writer = new PrintWriter(outStream);

        String transactionId = req.getParameter("trans_id");
        String error = req.getParameter("error");
        if(transactionId==null || transactionId.length()==0){
            writer.println("No transaction id!");
            writer.flush();
            return;
        }
        TransactionClient tc = TransactionClient.getInstance();
        ECOMMHelper ec = tc.getECOMM();
        TransactionRedirections redirections = tc.getRedirectionsForTransaction(transactionId);
        if(error!=null && error.length()>0){
            //An error occurred.
            TransactionResult tres = ec.getTransactionStatus(transactionId, true);
            tc.reportFailedTransaction(transactionId, error, tres);
            onTransactionFailed(resp, writer, redirections, tres);
        }else{
            tc.reportSuccessfullTransaction(transactionId);
            if(redirections.hasOk()){
                resp.sendRedirect(resp.encodeRedirectURL(redirections.getOnOk()));
            }else{
                writer.println("Success");
            }
        }
        writer.flush();
    }

}
