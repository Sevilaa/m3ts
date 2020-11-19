package com.android.grafika.initialize;

class InitializeFragmentFactory {

    private InitializeFragmentFactory(){
        // private constructor -> static class
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSpecifyMatchFragment.
     */
    static InitializeSpecifyMatchFragment newSpecifyMatchInstance() {
        return new InitializeSpecifyMatchFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeSelectingCornersFragment.
     */
    static InitializeSelectingCornersFragment newSelectingCornersInstance() {
        return new InitializeSelectingCornersFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeCreateMatchRoom.
     */
    static InitializeCreateMatchRoom newCreateRoomInstance() {
        return new InitializeCreateMatchRoom();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment InitializeDoneFragment.
     */
    static InitializeDoneFragment newDoneInstance() {
        return new InitializeDoneFragment();
    }

}
