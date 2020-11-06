package com.android.grafika.initialize;

import java.lang.ref.WeakReference;

class InitializeFragmentFactory {

    private InitializeFragmentFactory(){
        // private constructor -> static class
    }

    static InitializeSpecifyMatchFragment newSpecifyMatchInstance(InitializeActivity activity) {
        InitializeSpecifyMatchFragment fragment = new InitializeSpecifyMatchFragment();
        fragment.setActivityWeakReference(new WeakReference<>(activity));
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSelectingCornersFragment.
     */
    static InitializeSelectingCornersFragment newSelectingCornersInstance(InitializeActivity activity) {
        InitializeSelectingCornersFragment fragment = new InitializeSelectingCornersFragment();
        fragment.setActivityWeakReference(new WeakReference<>(activity));
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSelectingCornersFragment.
     */
    static InitializeDoneFragment newDoneInstance(InitializeActivity activity) {
        InitializeDoneFragment fragment = new InitializeDoneFragment();
        fragment.setActivityWeakReference(new WeakReference<>(activity));
        return fragment;
    }

}
