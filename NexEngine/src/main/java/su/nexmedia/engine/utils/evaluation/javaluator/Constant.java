package su.nexmedia.engine.utils.evaluation.javaluator;

/**
 * A constant in an expression.<br>
 * Some expressions need constants. For instance, it is impossible to perform
 * trigonometric calculus without using pi. A constant allows you to use
 * mnemonic in your expressions instead of the raw value of the constant. <br>
 * A constant for pi would be defined by:
 * <pre>
 *     {@code Constant<Double> pi = new Constant<Double>("pi");}
 * </pre>
 * With such a constant, you will be able to evaluate the expression "sin(pi/4)"
 *
 * @author Jean-Marc Astesana
 * @see <a href="../../../license.html">License information</a>
 */
public class Constant {

    private String name;

    /**
     * Constructor
     *
     * @param name The mnemonic of the constant. <br>
     *             The name is used in expressions to identify the constants.
     */
    public Constant(String name) {
        this.name = name;
    }

    /**
     * Gets the mnemonic of the constant.
     *
     * @return the id
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
