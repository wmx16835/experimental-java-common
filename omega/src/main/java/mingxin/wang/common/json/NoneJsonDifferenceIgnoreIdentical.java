package mingxin.wang.common.json;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
final class NoneJsonDifferenceIgnoreIdentical extends JsonDifference {
    static final NoneJsonDifferenceIgnoreIdentical INSTANCE = new NoneJsonDifferenceIgnoreIdentical();

    private NoneJsonDifferenceIgnoreIdentical() {
        super(Type.NONE);
    }
}
