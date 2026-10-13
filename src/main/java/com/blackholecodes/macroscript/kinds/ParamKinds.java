package com.blackholecodes.macroscript.kinds;

import com.blackholecodes.macroscript.mapping.ParamKind;

import java.util.EnumMap;
import java.util.Map;

/** The handler of each parameter kind. */
public final class ParamKinds {

    private static final ParamKinds INSTANCE = new ParamKinds();

    private final Map<ParamKind, ParamKindHandler> handlers = new EnumMap<>(ParamKind.class);

    private ParamKinds() {
        StringKind string = new StringKind();
        ObjectKind object = new ObjectKind();
        register(ParamKind.BOOL, new BoolKind());
        register(ParamKind.INT, new IntKind());
        register(ParamKind.DECIMAL, new DecimalKind());
        register(ParamKind.STRING, string);
        register(ParamKind.APP_NAME, string);
        register(ParamKind.TEXT, new TextKind());
        register(ParamKind.ENUM, new EnumKind());
        register(ParamKind.FLAGS, new FlagsKind());
        register(ParamKind.DURATION, new DurationKind());
        register(ParamKind.COLOR, new ColorKind());
        register(ParamKind.COLOR_LIST, new ColorListKind());
        register(ParamKind.VARIABLE, new VariableKind());
        register(ParamKind.VARIABLE_LIST, new VariableListKind());
        register(ParamKind.DICT_PATH, new DictPathKind());
        register(ParamKind.APP, new AppKind());
        register(ParamKind.APP_LIST, new AppListKind());
        register(ParamKind.CONTACT, new ContactKind());
        register(ParamKind.CONTACT_LIST, new ContactListKind());
        register(ParamKind.LIST, new ListKind());
        register(ParamKind.OBJECT, object);
        register(ParamKind.RAW_OBJECT, object);
        register(ParamKind.OBJECT_LIST, new ObjectListKind());
        register(ParamKind.HTTP_PARAMS, new HttpParamsKind());
        register(ParamKind.BLOCK_REF, new BlockRefKind());
        register(ParamKind.BLOCK_REF_LIST, new BlockRefListKind());
        register(ParamKind.TRIGGER_LIST, new TriggerListKind());
        register(ParamKind.CONDITION, new ConditionKind());
        register(ParamKind.STREAM_MAP, new StreamMapKind());
        register(ParamKind.FIXED, new FixedKind());
        for (ParamKind kind : ParamKind.values()) {
            if (!handlers.containsKey(kind)) {
                throw new IllegalStateException("No handler for parameter kind " + kind);
            }
        }
    }

    public static ParamKinds instance() {
        return INSTANCE;
    }

    public ParamKindHandler handler(ParamKind kind) {
        return handlers.get(kind);
    }

    private void register(ParamKind kind, ParamKindHandler handler) {
        handlers.put(kind, handler);
    }
}
