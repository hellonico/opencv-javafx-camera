package origami.booth;

import origami.Filter;

import java.lang.reflect.Field;

public abstract class FilterAction {
    private final String fieldName;


    public FilterAction(String fieldName) {
        this.fieldName = fieldName;
    }
    public Field getField(Filter fo) throws NoSuchFieldException {
        try {
            return fo.getClass().getDeclaredField(this.fieldName);
        } catch (Exception e) {
            return fo.getClass().getSuperclass().getDeclaredField(this.fieldName);
        }
    }
   abstract void apply(Filter fo) throws Exception;

    public static class Toggle extends FilterAction {

        public Toggle(String fieldName) throws NoSuchFieldException {
            super(fieldName);
        }

        public void apply(Filter fo) throws Exception {
            Field fi = super.getField(fo);
            fi.set(fo, !(boolean) fi.get(fo));
        }
    }

    public static class Inc extends FilterAction {
        double value;

        public Inc(String params0, String params1) throws NoSuchFieldException {
            super(params0);
            this.value = Double.parseDouble(params1);
        }

        public void apply(Filter fo) throws Exception {
            Field fi = super.getField(fo);

            Object fiv = fi.get(fo);

            if(fiv instanceof Double) {
                fi.setDouble(fo, fi.getDouble(fo)+value);
            } else {
                if(fiv instanceof Float) {
                        fi.setFloat(fo, (fi.getFloat(fo)+(float)value));
                    } else {
                        fi.setInt(fo, (fi.getInt(fo)+(int) value));
                    }
                }
            }
    }
}

