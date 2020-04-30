package org.bonitasoft.ca.eventhandler;

import static org.junit.Assert.*;

import org.bonitasoft.ca.model.RecipientFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RecipientFilterTest {
	

	@Test
	public void matches_starts_ok() {
		RecipientFilter f = new RecipientFilter("*@acme.com");
		assertTrue(f.matches("walter.bates@acme.com"));
	}

	@Test
	public void matches_starts_ko() {
		RecipientFilter f = new RecipientFilter("*@acme.com");
		assertFalse(f.matches("walter.bates@bonita.com"));
	}

	@Test
	public void matches_ends_ok() {
		RecipientFilter f = new RecipientFilter("walter*");
		assertTrue(f.matches("walter.bates@acme.com"));
	}

	@Test
	public void matches_ends_ko() {
		RecipientFilter f = new RecipientFilter("walter*");
		assertFalse(f.matches("chris.bates@acme.com"));
	}

	@Test
	public void matches_startsends_ok() {
		RecipientFilter f = new RecipientFilter("*kelly*");
		assertTrue(f.matches("hellen.kelly@acme.com"));
	}

	@Test
	public void matches_startsends_ko() {
		RecipientFilter f = new RecipientFilter("*kelly*");
		assertFalse(f.matches("walter.bates@acme.com"));
	}

	@Test
	public void matches_none_ok() {
		RecipientFilter f = new RecipientFilter("walter.bates@acme.com");
		assertTrue(f.matches("walter.bates@acme.com"));
	}

	@Test
	public void matches_none_ko() {
		RecipientFilter f = new RecipientFilter("walter.bates@acme.com");
		assertFalse(f.matches("walter.dubois@acme.com"));
	}

}
