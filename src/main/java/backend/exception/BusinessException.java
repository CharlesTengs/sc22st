package backend.exception;

public class BusinessException extends RuntimeException{

    private BusinessExceptionCode code;

    public BusinessException (BusinessExceptionCode code) {
        super(code.getDesc());
        this.code = code;
    }

    public BusinessExceptionCode getCode() {
        return code;
    }

    public void setCode(BusinessExceptionCode code) {
        this.code = code;
    }

    /**
     * Omitting stack trace for improved performance.
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
