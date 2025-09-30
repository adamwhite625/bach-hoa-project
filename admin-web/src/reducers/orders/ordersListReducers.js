const initialData = {
  rows: [],
  loading: false,
};

export default (state = initialData, { type, payload }) => {
  if (type === 'ORDERS_LIST_FETCH_STARTED') {
    return { ...state, loading: true };
  }
  if (type === 'ORDERS_LIST_FETCH_SUCCESS') {
    return { ...state, loading: false, rows: payload.rows, count: payload.count };
  }
  if (type === 'ORDERS_LIST_FETCH_ERROR') {
    return { ...state, loading: false, rows: [] };
  }
  return state;
};
