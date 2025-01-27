import lt.itakademija.exam.*;

public class CurrencyConverterImpl implements CurrencyConverter {
    private CurrencyRatesProvider currencyRatesProvider;

    public CurrencyConverterImpl(CurrencyRatesProvider currencyRatesProvider) {
        this.currencyRatesProvider = currencyRatesProvider;
    }

    @Override
    public Money convert(Currency currencyFrom, Currency currencyTo, Money money) {
        Money rate = currencyRatesProvider.getRate(currencyFrom, currencyTo);
        if(rate == null) {
            throw new CurrencyConversionException("no rate for specified currencies");
        }
        return money.multiply(rate);
    }
}
