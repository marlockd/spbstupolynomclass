package kotlinTasks.polynom
import java.lang.Math.pow

fun otl(po: Polynom) {
    po.elements.forEach {
        println(it.coef)
        println(it.pow)
        println()
    }
}


/**
 * Поправки к общему стандарту
 * Класс теперь будет обращаться к членам со степенью 0 как к реальой степени только при конвертации со строки в полином
 * Далее же степень 0 во всем коде означает ничто иное как отсутствие икса
 * Например: Как отличить члены "x" и "1" между собой? Ведь убрав из свойства наличие икса,
 * мы получим такое равенство, (coef = 1 pow = 1) которое одновременно полностью применимо и члену "1" и члену "x"
 * Поэтому, было решено сделать число ноль в степени показателя того, что в члене отсутствует неизвестный X,
 * за счет чего эта проблема становится не актуальной, а те участки кода, в которых теоритически возможно получение
 * нуля в степени во время тех или иных действий, идёт автоматическая замена этого члена на единицу
 */



fun convert(poly: String): Polynom {
    if (!Regex("""([-+]?((\d+x\^\d+)|(\d+x)|(x\^\d+)|\d+|x))+""").matches(poly))
        throw IllegalArgumentException()
    val tempList = mutableListOf<PolyPart>()
    val list = Regex("""-""").replace(Regex("""\+""").
            replace(poly, " "), " -").split(" ")
    list.forEach {
        if (it.matches(Regex("""-?\d+x\^\d+"""))) {
            val parts = it.split("x^").map { it.toDouble() }
            if (parts.last() == 0.0) tempList.add(PolyPart(1.0, 0))
            else tempList.add(PolyPart(parts.first(), parts.last().toInt()))
        }
        if (it.matches(Regex("""-?x\^\d+"""))) {
            val parts = it.split("x^")
            if (parts.last().toInt() == 0) tempList.add(PolyPart(1.0, 0))
            else if (parts.first() == "") tempList.add(PolyPart(1.0, parts.last().toInt()))
            else if (parts.first() == "-") tempList.add(PolyPart(-1.0, parts.last().toInt()))
        }
        if (it.matches(Regex("""-?\d+\^\d+"""))) {
            val parts = it.split("^")
            if (parts.last().toInt() == 0) tempList.add(PolyPart(1.0, 0))
            else tempList.add(PolyPart(pow(parts.first().toDouble(), parts.last().toDouble()), parts.last().toInt()))
        }
        if (it.matches(Regex("""-?\d+x"""))) tempList.add(PolyPart(it.replace("x", "").toDouble(), 1))
        if (it.matches(Regex("""-?\d+"""))) tempList.add(PolyPart(it.toDouble(), 0))
        if (it == "x") tempList.add(PolyPart(1.0, 1))
        if (it == "-x") tempList.add(PolyPart(-1.0, 1))
    }
    return Polynom(tempList).shorten()
}

/**
// отладка полинома
fun out(po: Polynom) {
    val res = mutableListOf<String>()
    fun sOut(pp: PolyPart): String {
        if (pp.isX && pp.pow > 1) return pp.coef.toString() + "x^" + pp.pow.toString()
        if (!pp.isX) return pow(pp.coef, pp.pow.toDouble()).toString()
        if (pp.isX && pp.pow == 1) return pp.coef.toString() + "x"
        return ""
    }
    res.add(sOut(po.elements.first()))
    if (po.elements.size > 1)
        for (i in 1 until po.elements.size) {
            if (po.elements[i].coef > 0) res.add("+")
            res.add(sOut(po.elements[i]))
        }
    println(res.joinToString(""))
}
*/

data class Polynom(val elements: MutableList<PolyPart>) {

    // сравнение полиномов между собой
    fun isEqual(other: Polynom): Boolean = elements == other.elements

    // замена знаков в полиноме
    fun changeSign(): Polynom = Polynom(elements.map { PolyPart(it.coef * -1, it.pow) }.toMutableList())

    // вычисление при определенном целом значении X
    fun calcX(x: Int): Double {
        val sumList = mutableListOf<Double>()
        elements.forEach {
            if (it.pow != 0) sumList.add(it.coef * pow(x.toDouble(), it.pow.toDouble()))
            else sumList.add(it.coef * x)
        }
        return sumList.sum()
    }

    // упрощение полинома
    fun shorten(): Polynom {
        val res = mutableListOf<PolyPart>()
        val subParts = mutableMapOf<Int, MutableList<Double>>()
        elements.forEach {
            if (subParts[it.pow].isNullOrEmpty()) subParts[it.pow] = mutableListOf(it.coef)
            else subParts[it.pow]!!.add(it.coef)
        }
        subParts.forEach { res.add(PolyPart(it.value.sum(), it.key)) }
        return Polynom(res.filter { it.coef != 0.0 }.sortedBy { it.pow }.toMutableList())
    }

    // сложение полиномов
    fun addition(other: Polynom): Polynom {
        val res = mutableListOf<PolyPart>()
        val subList = mutableMapOf<Int, MutableList<Double>>()
        elements.forEach {
            if (subList[it.pow].isNullOrEmpty()) subList[it.pow] = mutableListOf(it.coef)
            else subList[it.pow]!!.add(it.coef)
        }
        other.elements.forEach {
            if (subList[it.pow].isNullOrEmpty()) subList[it.pow] = mutableListOf(it.coef)
            else subList[it.pow]!!.add(it.coef)
        }
        subList.forEach { res.add(PolyPart(it.value.sum(), it.key)) }
        return Polynom(res).shorten()
    }

    // вычитание полиномов
    fun subtract(other: Polynom): Polynom = addition(other.changeSign()).shorten()

    // умножение
    fun multiply(other: Polynom): Polynom {
        val subParts = mutableListOf<Polynom>()
        for (i in 0 until elements.size) {
            subParts.add(Polynom(mutableListOf()))
            other.elements.forEach { its -> subParts[i].elements.add(elements[i].multiply(its)) }
        }
        var res = subParts.first()
        for (j in 1 until subParts.size) { res = res.addition(subParts[j]) }
        return res.shorten()
    }

    // деление
    fun div(other: Polynom): Polynom = Polynom(elements).commonDivide(other).first

    // остаток от деления
    fun mod(other: Polynom): Polynom = Polynom(elements).commonDivide(other).second

    // общее деление
    /**
     * Суть деления такова: есть делимое(dividend), есть делитель (divider)
     * quotient - частное, tempPoly - временный полином умножения деления старших степенных членов на делитель
     * а частное - оставшееся делимое после всех итераций
     *
     * Как считается кол-во итераций?
     * Заметил, что количество итераций высчитываетя по разности степеней старших членов делимого и делителя + 1
     */
    fun commonDivide(divider: Polynom): Pair<Polynom, Polynom> {
        var dividend = Polynom(elements)
        val quotient = mutableListOf<PolyPart>()
        val times = dividend.elements.maxBy { it.pow }!!.pow - divider.elements.maxBy { it.pow }!!.pow + 1
        repeat(times) {
            val tempPoly = mutableListOf<PolyPart>()
            val higherDividend = dividend.elements.maxBy { it.pow }!!
            val higherDivider = divider.elements.maxBy { it.pow }!!
            quotient.add(higherDividend.divide(higherDivider))
            for (i in 0 until divider.elements.size) tempPoly.add(divider.elements[i].multiply(higherDividend.divide(higherDivider)))
            dividend = dividend.subtract(Polynom(tempPoly))
        }
        return Pair(Polynom(quotient).shorten(), dividend.shorten())
    }


}

data class PolyPart(var coef: Double, val pow: Int) {

    fun multiply(otherPart: PolyPart): PolyPart {
        if ((pow != 0) && (otherPart.pow != 0)) return PolyPart(coef * otherPart.coef, pow + otherPart.pow)
        if ((pow != 0) && (otherPart.pow == 0)) return PolyPart(coef * otherPart.coef, pow)
        if ((pow == 0) && (otherPart.pow != 0)) return PolyPart(coef * otherPart.coef, otherPart.pow)
        if ((pow == 0) && (otherPart.pow == 0)) return PolyPart(coef * otherPart.coef,0)
        throw IllegalArgumentException()
    }

    fun divide(otherPart: PolyPart): PolyPart {
        if (pow != 0 && otherPart.pow != 0 && (pow >= otherPart.pow))
            return PolyPart(coef / otherPart.coef,pow - otherPart.pow)
        throw IllegalArgumentException()
    }


}

