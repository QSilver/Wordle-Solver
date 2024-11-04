import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PatternTest {
    @Test
    public void test() {
        assertEquals(0, EntropySolver.calculatePattern("oater", "cynic"));
        assertEquals(4, EntropySolver.calculatePattern("sulci", "cynic"));
        assertEquals(49, EntropySolver.calculatePattern("mincy", "cynic"));
        assertEquals(242, EntropySolver.calculatePattern("cynic", "cynic"));
        assertEquals(39, EntropySolver.calculatePattern("soare", "aroma"));
        assertEquals(120, EntropySolver.calculatePattern("maron", "aroma"));
    }
}
