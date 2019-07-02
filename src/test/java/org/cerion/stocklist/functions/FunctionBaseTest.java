package org.cerion.stocklist.functions;

import org.cerion.stocklist.Utils;
import org.cerion.stocklist.arrays.BandArray;
import org.cerion.stocklist.arrays.ValueArray;
import org.cerion.stocklist.functions.types.IFunctionEnum;
import org.cerion.stocklist.functions.types.Indicator;
import org.cerion.stocklist.functions.types.Overlay;
import org.cerion.stocklist.functions.types.PriceOverlay;
import org.cerion.stocklist.overlays.BollingerBands;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class FunctionBaseTest extends FunctionTestBase {

    @Test
    public void hashCodeUniqueness() {
        List<IFunctionEnum> values = new ArrayList<>();
        values.addAll(Arrays.asList(Overlay.values()));
        values.addAll(Arrays.asList(PriceOverlay.values()));
        values.addAll(Arrays.asList(Indicator.values()));

        int size = values.size();
        Map<IFunction, String> map = new HashMap<>();

        for(IFunctionEnum f : values) {
            IFunction function = f.getInstance();
            //TODO, create multiple versions variations of default values
            map.put(function, "");
        }

        assertEquals("no values returned", true, size > 0);
        assertEquals("map does not match size", size, map.size());
    }

    @Test
    public void hashCodeUnique_WithSameOrdinal() {
        Overlay overlay = Overlay.values()[0];
        PriceOverlay po = PriceOverlay.values()[0];
        Map<IFunction, String> map = new HashMap<>();

        assertEquals("expected functions do not match", overlay.ordinal(), po.ordinal());
        Number[] params = new Number[]{ 0 };

        IFunction call1 = overlay.getInstance(params);
        IFunction call2 = po.getInstance(params);
        IFunction call3 = overlay.getInstance(params);
        IFunction call4 = po.getInstance(params);

        assertEquals("hash code should match", call1.hashCode(), call2.hashCode());

        map.put(call1, "");
        map.put(call2, "");
        assertEquals("unique functions mapped to same value", map.size(), 2);

        map.put(call3, "");
        map.put(call4, "");
        assertEquals("same functions should not be mapped", map.size(), 2);
    }

    @Test
    public void equals_checksParameters() {
        IFunction call1 = Overlay.EMA.getInstance(10);
        IFunction call2 = Overlay.EMA.getInstance(10);
        IFunction call3 = Overlay.EMA.getInstance(20);

        assertEquals("should be equal", call1, call2);
        assertNotEquals("should not be equal", call1, call3);
    }

    @Test
    public void parametersVerified_DecimalType() {
        //All types should work on decimal input, no exceptions thrown
        IFunction call = new BollingerBands(20, 2.0f);
        call.eval(Utils.generateList(50));

        call = new BollingerBands(20, 2.0d);
        call.eval(Utils.generateList(50));

        //Int
        call = new BollingerBands(20, 2);
        call.eval(Utils.generateList(50));
    }

    /* Type checked now so not necessary to test
    @Test(expected = IllegalArgumentException.class)
    public void parametersVerified_MissingParameter() {
        IFunction call = new BollingerBands(20);
        call.eval(Utils.generateList(50));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parametersVerified_TooManyParameters() {
        IFunction call = new BollingerBands(20, 2.0, 10);
        call.eval(Utils.generateList(50));
    }
    */

    @Test(expected = IllegalArgumentException.class)
    public void parametersVerified_setParams_countMismatch() {
        IFunction call = new BollingerBands(20, 2.0);
        call.setParams(20,10,10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void parametersVerified_setParams_typeMismatch() {
        IFunction call = new BollingerBands(20, 2.0);
        call.setParams(20,10);
    }

    @Test
    public void verifyReturnTypes_simpleOverlays() {

        for(Overlay o : Overlay.values()) {
            ISimpleOverlay overlay = o.getInstance();
            ValueArray arr = overlay.eval(mPriceList.getClose());

            Class<?> c = arr.getClass();
            if (arr instanceof BandArray)
                c = BandArray.class;

            assertEquals("'" + o.toString() + "' resultType() does not match eval() result", c, overlay.getResultType());

            // Verify when called on both evals
            arr = overlay.eval(mPriceList);
            if (arr instanceof BandArray)
                c = BandArray.class;
            assertEquals("'" + o.toString() + "' resultType() does not match eval() result (2)", c, overlay.getResultType());
        }
    }

    @Test
    public void verifyReturnTypes_priceOverlays() {
        for(PriceOverlay o : PriceOverlay.values()) {
            IPriceOverlay overlay = o.getInstance();
            ValueArray arr = overlay.eval(mPriceList);

            Class<?> clazz = arr.getClass();
            if (arr instanceof BandArray)
                clazz = BandArray.class;

            assertEquals("'" + o.toString() + "' resultType() does not match eval() result", clazz, overlay.getResultType());
        }
    }

    @Test
    public void verifyReturnTypes_indicators() {
        for(Indicator i : Indicator.values()) {
            IIndicator indicator = i.getInstance();
            ValueArray arr = indicator.eval(mPriceList);

            assertEquals("'" + i.toString() + "' resultType() does not match eval() result", arr.getClass(), indicator.getResultType());
        }
    }


}