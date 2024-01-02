package Model;

import java.text.DecimalFormat;

public class ComparePurchase {

    private final String Symbol;
    private final int Quantity;
    private final Float buyPrice;
    private final Float nowPrice;
    private final Float difference;
    private final String TransactionDate;

    public ComparePurchase(String symbol, int quantity, Float buyPrice, Float nowPrice, String TransactionDate) {
        this.Symbol = symbol;
        this.Quantity = quantity;
        this.buyPrice = buyPrice;
        this.nowPrice = nowPrice;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        this.difference = Float.parseFloat(decimalFormat.format((1 - (buyPrice / nowPrice)) * 100));
        this.TransactionDate = TransactionDate;
    }

    public String getSymbol() {
        return Symbol;
    }

    public int getQuantity() {
        return Quantity;
    }

    public Float getBuyPrice() {
        return buyPrice;
    }

    public Float getNowPrice() {
        return nowPrice;
    }

    public Float getDifference() {
        return difference;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    @Override
    public String toString() {
        return "Symbol:" + Symbol +
                ",Quantity:" + Quantity +
                ",buyPrice:" + buyPrice +
                ",nowPrice:" + nowPrice +
                ",difference:" + difference +
                ",TransactionDate:" + TransactionDate;
    }
}
