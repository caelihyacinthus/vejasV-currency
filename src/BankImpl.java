import lt.itakademija.exam.*;

import java.util.ArrayList;
import java.util.List;

public class BankImpl implements Bank {
    private List<Customer> customers = new ArrayList<>();
    private SequenceGenerator customerIDGen = new SequenceGenerator();
    private SequenceGenerator accountIDGen = new SequenceGenerator();
    private SequenceGenerator transactionIDGen = new SequenceGenerator();
    private CurrencyConverter currencyConverter;

    public BankImpl(CurrencyConverter currencyConverter) {
        this.currencyConverter = currencyConverter;
    }

    @Override
    public Customer createCustomer(PersonCode personCode, PersonName personName) {
        List<PersonCode> customersCodes = customers.stream().map(Customer::getPersonCode).toList();
        if (customersCodes.contains(personCode)) {
            throw new CustomerCreateException("Customer already exists with person code");
        }

        if (personCode == null || personName == null) {
            throw new NullPointerException("null args not allowed");
        }

        Customer customer = new Customer(customerIDGen.getNext(), personCode, personName);
        customers.add(customer);
        return customer;
    }

    @Override
    public Account createAccount(Customer customer, Currency currency) {
        if (customer == null || currency == null) {
            throw new NullPointerException("null args not allowed");
        }
        if (!customers.contains(customer)) {
            throw new AccountCreateException("Customer does not exists");
        }
        Account account = new Account(accountIDGen.getNext(), customer, currency, new Money(0.0));
        customer.addAccount(account);
        return account;
    }

    @Override
    public Operation transferMoney(Account debitAccount, Account creditAccount, Money debitAmount) {
        Money debitMoney = debitAccount.getBalance();
        if (debitMoney.isLessThan(debitAmount)) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        debitAccount.setBalance(debitMoney.substract(debitAmount));

        if (debitAccount.getCurrency().equals(creditAccount.getCurrency())) {
            creditAccount.setBalance(creditAccount.getBalance().add(debitAmount));
        } else {
            Money convertedDebitAmount = currencyConverter.convert(debitAccount.getCurrency(), creditAccount.getCurrency(), debitAmount);
            creditAccount.setBalance(creditAccount.getBalance().add(convertedDebitAmount));
        }

        return new Operation(transactionIDGen.getNext(), debitAccount, creditAccount, debitAmount);
    }

    @Override
    public Money getBalance(Currency currency) {
        Money total = new Money(0.0);

        for (Customer c : customers) {
            for (Account a : c.getAccounts()) {
                if (a.getCurrency().equals(currency)) {
                    total = total.add(a.getBalance());
                } else {
                    Money mon = currencyConverter.convert(a.getCurrency(), currency, a.getBalance());
                    total = total.add(mon);
                }
            }
        }
        return total;
    }
}
