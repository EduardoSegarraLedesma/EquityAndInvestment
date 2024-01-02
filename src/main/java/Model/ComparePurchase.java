package Model;

import java.text.DecimalFormat;

public class ComparePurchase {

    private final String Symbol;
    private final int Quantity;
    private final String buyPrice;
    private final String nowPrice;
    private final String difference;
    private final String TransactionDate;

    public ComparePurchase(String symbol, int quantity, Float buyPrice, Float nowPrice, String TransactionDate) {
        this.Symbol = symbol;
        this.Quantity = quantity;
        this.buyPrice = "$" + buyPrice.toString();
        this.nowPrice = "$" + nowPrice.toString();
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        this.difference = Float.parseFloat(decimalFormat.format((1 - (buyPrice / nowPrice)) * 100)) + "%";
        this.TransactionDate = TransactionDate;
    }

    public String getSymbol() {
        return Symbol;
    }

    public int getQuantity() {
        return Quantity;
    }

    public String getBuyPrice() {
        return buyPrice;
    }

    public String getNowPrice() {
        return nowPrice;
    }

    public String getDifference() {
        return difference;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    @Override
    public String toString() {
        return "{Symbol=" + Symbol +
                ", Quantity=" + Quantity +
                ", buyPrice=" + buyPrice +
                ", nowPrice=" + nowPrice +
                ", difference=" + difference +
                ", TransactionDate=" + TransactionDate + "}";
    }
}
