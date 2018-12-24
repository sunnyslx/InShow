package com.idx.inshowapp.utils;

import com.idx.inshowapp.data.InShowFilter;
import com.idx.inshowapp.filter.FilterTypeFactory;
import com.idx.inshowapp.filter.InShowFilterType;


import java.util.HashMap;

/**
 * Created by Sunny on 18-9-15.
 */

public class FilterUtils {

    public static HashMap<String ,InShowFilter> getFilter(){
        HashMap<String,InShowFilter> filterHashMap=new HashMap<>();
        InShowFilterType[] inShowFilterTypes = new InShowFilterType[]{
                InShowFilterType.NONE,
                InShowFilterType.SAKURA,
                InShowFilterType.ROMANCE,
                InShowFilterType.RETRO,
                InShowFilterType.SWEET,
                InShowFilterType.BALCKWHITE,
                InShowFilterType.LATTE
        };
        String[] nameArray=new String[inShowFilterTypes.length];
        for (int i=0;i<inShowFilterTypes.length;i++){
            InShowFilter showFilter=new InShowFilter();
            nameArray[i]=FilterTypeFactory.filterTypeName(inShowFilterTypes[i]);
            showFilter.setFilterName(FilterTypeFactory.filterTypeName(inShowFilterTypes[i]));
            showFilter.setGpuImageFilter(FilterTypeFactory.initFilters(inShowFilterTypes[i]));
            showFilter.setInShowFilterType(inShowFilterTypes[i]);
            filterHashMap.put(nameArray[i],showFilter);
        }
        return filterHashMap;
    }
}
