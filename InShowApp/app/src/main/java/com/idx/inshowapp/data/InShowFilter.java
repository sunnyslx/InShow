package com.idx.inshowapp.data;


import com.idx.inshowapp.filter.InShowFilterType;
import com.idx.inshowapp.filter.base.GPUImageFilter;

/**
 * Created by Sunny on 18-9-15.
 */

public class InShowFilter {
    private int filterId;
    private String filterName;
    private InShowFilterType inShowFilterType;
    private GPUImageFilter gpuImageFilter;


    public InShowFilterType getInShowFilterType() {
        return inShowFilterType;
    }

    public void setInShowFilterType(InShowFilterType inShowFilterType) {
        this.inShowFilterType = inShowFilterType;
    }

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(int filterId) {
        this.filterId = filterId;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public GPUImageFilter getGpuImageFilter() {
        return gpuImageFilter;
    }

    public void setGpuImageFilter(GPUImageFilter gpuImageFilter) {
        this.gpuImageFilter = gpuImageFilter;
    }
}
