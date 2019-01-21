package bg.icafe.network.http;

import bg.icafe.Config;
import bg.icafe.network.mq.TransactionClient;
import bg.icafe.payment.ECOMMHelper;
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
            handleFailedTransaction(req, resp);
        }
    }

    private void handleFailedTransaction(HttpServletRequest req, HttpServletResponse resp) {

    }

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
        if(error!=null && error.length()>0){
            //An error occurred.
            tc.reportFailedTransaction(transactionId, error);
            TransactionResult tres = ec.getTransactionStatus(transactionId, true);
            writer.println("Transaction failed");
            writer.print("Reason: ");
            switch(tres.getResult()){
                case Declined:
                    writer.println("Declined");
                    break;
                default:
                    writer.println("Unknown");
                    break;
            }
        }else{
            tc.reportSuccessfullTransaction(transactionId);
        }
        writer.flush();
    }

}
