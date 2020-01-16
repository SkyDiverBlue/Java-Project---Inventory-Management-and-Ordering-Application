package papapizza.office;

import org.javamoney.moneta.Money;
import org.salespointframework.accountancy.Accountancy;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.core.DataInitializer;
import org.salespointframework.time.BusinessTime;
import org.springframework.util.Assert;

import java.time.Duration;

class OfficeInitializer implements DataInitializer {

	private final Accountancy accountancy;
	private final OfficeController officeController;
	private final BusinessTime businessTime;

	public OfficeInitializer(Accountancy accountancy, OfficeController officeController, BusinessTime businessTime) {
		Assert.notNull(accountancy, "accountancy should not be null");
		this.accountancy = accountancy;
		this.officeController = officeController;
		this.businessTime = businessTime;
	}

	@Override
	public void initialize() {
		businessTime.forward(Duration.ofDays(-200));
		accountancy.add(new AccountancyEntry(Money.of(-2830, "EUR"), "Neue Mitarbeiter und Zutaten gekauft"));
		accountancy.add(new AccountancyEntry(Money.of(34, "EUR"), "Enteignung der 'Mitarbeiter'"));
		businessTime.reset();
		businessTime.forward(Duration.ofDays(-60));
		accountancy.add(new AccountancyEntry(Money.of(100, "EUR"), "erste Bestellungen"));
		businessTime.forward(Duration.ofDays(50));
		accountancy.add(new AccountancyEntry(Money.of(-100, "EUR"), "Lager aufgefüllt"));
		accountancy.add(new AccountancyEntry(Money.of(-42, "EUR"), "Gauda aus Gauda gekauft"));
		businessTime.forward(Duration.ofDays(2));
		accountancy.add(new AccountancyEntry(Money.of(35.20, "EUR"), "Bestellung"));
		accountancy.add(new AccountancyEntry(Money.of(-14.66, "EUR"), "Salami aufgefüllt"));
		businessTime.reset();

	}
}

