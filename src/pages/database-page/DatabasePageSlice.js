import { createSlice } from "@reduxjs/toolkit";
import { StateWithLoader } from "../../utils/StoreUtils";
import LoadingState from "../../enums/LoadingState";

const initialState = {
  users: new StateWithLoader([], LoadingState.LOADING),
};

export const databasePageSlice = createSlice({
  name: "DatabasePage",
  initialState,
  reducers: {
    updateItems: (state, action) => {
      state.users = new StateWithLoader(action.payload, LoadingState.LOADED);
    },
    updateUser: (state, action) => {
      state.users.value.forEach(function (user) {
        if (user.id === action.payload.id) {
          user[action.payload.key] = action.payload.value;
        }
      });
      console.log(state.users);
    },
    setIsLoadingItems: (state) => {
      state.users = new StateWithLoader([], LoadingState.LOADING);
    },
    setErrorLoadingItems: (state) => {
      state.users = new StateWithLoader([], LoadingState.ERROR);
    },
  },
});

export const {
  updateItems,
  updateUser,
  setIsLoadingItems,
  setErrorLoadingItems,
} = databasePageSlice.actions;

export default databasePageSlice.reducer;
