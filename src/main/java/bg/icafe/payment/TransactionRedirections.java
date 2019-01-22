package bg.icafe.payment;

public class TransactionRedirections{
    private String onFailed;
    private String onOk;
    public TransactionRedirections(String onFailed, String onOk){
        this.onFailed = onFailed;
        this.onOk = onOk;
    }

    public String getOnFailed() {
        return onFailed;
    }

    public String getOnOk() {
        return onOk;
    }

    /**
     * Whether this redirection has an url to redirect to, when it fails.
     * @return
     */
    public boolean hasFailed() {
        return onFailed!=null && onFailed.length()>0;
    }

    public boolean hasOk() {
        return onOk!=null && onOk.length()>0;
    }
}
