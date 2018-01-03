package com.salcedo.rapbot.driver;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class OpenClosedRangeTest {
    private Range range;

    @Before
    public void setUp() throws Exception {
        range = new OpenClosedRange(true, true, 1, 100);
    }

    @Test
    public void modulo() {
        assertThat(range.modulo(-10), is(equalTo(91)));
    }

    @Test
    public void bounded() {
        assertThat(range.bounded(-10), is(equalTo(1)));
        assertThat(range.bounded(101), is(equalTo(100)));
    }
}