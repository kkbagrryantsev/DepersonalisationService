import { call, put, takeEvery } from "redux-saga/effects";
import { execApiCall } from "../../utils/ApiUtils";
import {
  downloadSpecificType,
  getDepersonalisedUsers,
  getUsers,
  updatePeople,
} from "../../api/ApiCalls";
import { createErrorToast, createSuccessToast } from "../../models/ToastModel";
import {
  uploadDepersonalisedUsers,
  uploadUsers,
  synchronizeUsers,
  downloadFileType,
} from "./DatabasePageActions";
import { updateItems } from "./DatabasePageSlice";
import { store } from "../../store/Store";
import { downloadFile } from "../../utils/BrowserUtils";

export function* databasePageSagaWatcher() {
    yield takeEvery(uploadUsers, sagaGetUsers);
    yield takeEvery(uploadDepersonalisedUsers, sagaGetDepersonalisedUsers);
    yield takeEvery(synchronizeUsers, sagaSynchronizeUsers);
    yield takeEvery(downloadFileType, sagaDownloadFile);
}

function* sagaGetUsers() {
  yield call(execApiCall, {
    mainCall: () => getUsers(),
    *onSuccess(response) {
      createSuccessToast(`Данные получены`);
      yield put(updateItems(response.data));
    },
    onAnyError() {
      createErrorToast(`Ошибка сервера`);
    },
  });
}

function* sagaGetDepersonalisedUsers() {
  yield call(execApiCall, {
    mainCall: () => getDepersonalisedUsers(),
    *onSuccess(response) {
      createSuccessToast(`Данные получены`);
      yield put(updateItems(response.data));
    },
    onAnyError() {
      createErrorToast(`Что-то пошло не так`);
    },
  });
}

function* sagaSynchronizeUsers() {
  const users = store.getState().databasePage.users.value;
  yield call(execApiCall, {
    mainCall: () => updatePeople(users),
    onSuccess() {
      createSuccessToast(`Синхронизация данных прошла успешно`);
    },
    onAnyError() {
      createErrorToast(`Что-то пошло не так`);
    },
  });
}

function* sagaDownloadFile(filetype) {
  console.log(filetype)
  yield call(execApiCall, {
    mainCall: () => downloadSpecificType(filetype.payload),
    onSuccess(response) {
      downloadFile(response.data, "data." + filetype.payload);
    },
    onAnyError() {
      createErrorToast(`Не удалось скачать данные`);
    },
  });
}