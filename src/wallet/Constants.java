package wallet;
public class Constants {

    public enum AuthResult {
        AUTH_ERROR,
        ADMINISTRATOR,
        USER
    }
    
    public enum WalletMode {
        EXPENSES,
        INCOME
    }
    
    public enum ResultSetState {
        MORE_THAN_ONE,
        ONLY_ONE,
        EMPTY
    }    
}
