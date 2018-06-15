package mingxin.wang.common.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Copyright (c) 2017-2018 Mingxin Wang. All rights reserved.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public final class SheetData {
    private String name;
    private String[][] data;
}
