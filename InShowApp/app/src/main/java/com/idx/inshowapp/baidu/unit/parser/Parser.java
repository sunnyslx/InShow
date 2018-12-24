/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.idx.inshowapp.baidu.unit.parser;


import com.idx.inshowapp.baidu.unit.exception.UnitError;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws UnitError;
}
