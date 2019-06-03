package ru.spbstu.kotlinclass

import org.junit.Test
import org.junit.Assert.*

class PolynomTest {

    private val poly1 = convert("7x^4+3x^3-6x^2+x-8")
    private val poly2 = convert("172x^4+34x-6x^2+x^4-x-4-8+2")
    private val poly3 = convert("2x^3+x^4-2x^2-x")
    private val poly4 = convert("4x^6+2x^7-4x^6-2x^4")
    private val poly5 = convert("10x^5+3x^4-12x^3+25x^2-2x+5")
    private val poly6 = convert("5x^2-x+2")
    private val poly7 = convert("2x^7+x^8-2x^6-x^5")
    private val poly8 = convert("x^8+4x^7+0x^6-9x^5+2x^4+2x^3")

    @Test
    fun convert() {
        assertEquals(Polynom(emptyList()), convert("-0x^3"))
        assertEquals(Polynom(listOf(PolyPart(111.0, 0))),
                convert("5^2-0x^3+6x^0-8^0+9^2"))
        assertEquals(Polynom(listOf(
                PolyPart(7.0, 4),
                PolyPart(3.0, 3),
                PolyPart(-6.0, 2),
                PolyPart(1.0, 1),
                PolyPart(-8.0, 0)
        )).shorten(), convert("7x^4+3x^3-6x^2+x-8"))
        assertEquals(Polynom(listOf(
                PolyPart(5.0, 2),
                PolyPart(-1.0, 1),
                PolyPart(2.0, 0)
        )).shorten(), convert("5x^2-x+2"))
        assertEquals(Polynom(listOf(
                PolyPart(2.0, 7),
                PolyPart(1.0, 8),
                PolyPart(-2.0, 6),
                PolyPart(-1.0, 5)
        )).shorten(), convert("2x^7+x^8-2x^6-x^5"))
        assertEquals(Polynom(listOf(PolyPart(2.0, 0))), convert("2"))

    }


    @Test
    fun isEqual() {
        assertTrue(poly1.isEqual(poly1))
        assertTrue(poly3.isEqual(poly3))
        assertFalse(poly1.isEqual(poly2))
        assertFalse(poly2.isEqual(poly6))
    }

    @Test
    fun changeSign() {
        assertEquals(convert("-2x^7-x^8+2x^6+x^5"), poly7.changeSign())
        assertEquals(convert("-7x^4-3x^3+6x^2-x+8"), poly1.changeSign())
        assertEquals(convert("-172x^4-34x+6x^2-x^4+x+4+8-2"), poly2.changeSign())
        assertEquals(convert("-2x^3-x^4+2x^2+x"), poly3.changeSign())
    }

    @Test
    fun calcX() {
        // Были ошибки в написании результатов вычислений, но уже исправлены
        // полной проверкой через wolframAlpha, а так же добавлены новые условия
        assertEquals(106.0, poly1.calcX(2), 1e-5)
        assertEquals(14048.0, poly2.calcX(3), 1e-5)
        assertEquals(2982.0, poly3.calcX(7), 1e-5)
        assertEquals(224.0, poly4.calcX(2), 1e-5)
        assertEquals(5.0, poly5.calcX(0), 1e-5)
        assertEquals(480168.0, convert("4x^10-4x^7+2x^11-4x^8-4x^9+4x^6+2x^5").calcX(3), 1e-5)
        assertEquals(-2.0, convert("2x^3+x^4-2x^2-x").calcX(-1), 1e-5)
        assertEquals(8.0, convert("4x^10-4x^7+2x^11-4x^8-4x^9+4x^6+2x^5").calcX(-1), 1e-5)
        assertEquals(1728.0, convert("4x^10-4x^7+2x^11-4x^8-4x^9+4x^6+2x^5").calcX(-2), 1e-5)
    }

    @Test
    fun addition() {
        assertEquals(convert("14x^4+6x^3-12x^2+2x-16"), poly1.addition(poly1))
        assertEquals(convert("176x^4+31x+19x^2-5+10x^5-12x^3"), poly2.addition(poly5))
        assertEquals(convert("6x^7+1x^8-9x^5+2x^3"), poly4.addition(poly8))
        assertEquals(convert("171x^4+33x-6x^2-10+2x^7"), poly2.addition(poly4))
    }

    @Test
    fun subtract() {
        assertEquals(convert("-166x^4+3x^3-32x+2"), poly1.subtract(poly2))
        assertEquals(convert("2x^3+3x^4-2x^2-1x-2x^7"), poly3.subtract(poly4))
        assertEquals(convert("10x^5+3x^4-12x^3+20x^2-1x+3"), poly5.subtract(poly6))
        assertEquals(convert("-2x^7-2x^6+8x^5-2x^4-2x^3"), poly7.subtract(poly8))
    }

    @Test
    fun multiply() {
        assertEquals(convert("1211x^8+386x^5-1080x^6-1319x^4+519x^7-234x^3+141x^2-274x+80"), poly1.multiply(poly2))
        assertEquals(convert("4x^10-4x^7+2x^11-4x^8-4x^9+4x^6+2x^5"), poly3.multiply(poly4))
        assertEquals(convert("50x^7+5x^6-43x^5+143x^4-59x^3+77x^2-9x+10"), poly5.multiply(poly6))
        assertEquals(convert("x^16+6x^15+6x^14-18x^13-20x^12+24x^11+9x^10-6x^9-2x^8"), poly7.multiply(poly8))
    }

    @Test
    fun div() {
        assertEquals(convert("2x^3+1x^2-3x+4"), poly5.div(poly6))
        assertEquals(convert("4x^2-20x+102"), convert("4x^3+2x-11").div(convert("x+5")))
        assertEquals(Polynom(emptyList()),
                convert("7x^3+9x^2-5x+9").div(convert("5x^7+10x^6-17x^2+14x-7")))
    }

    @Test
    fun mod() {
        assertEquals(convert("8x-3"), poly5.mod(poly6))
        assertEquals(convert("-521"), convert("4x^3+2x-11").mod(convert("x+5")))
        assertEquals(convert("7x^3+9x^2-5x+9"),
                convert("7x^3+9x^2-5x+9").mod(convert("5x^7+10x^6-17x^2+14x-7")))
    }

}