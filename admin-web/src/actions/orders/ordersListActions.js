import axios from 'axios';
import Errors from 'components/FormItems/error/errors';

async function list() {
  const response = await axios.get(`/orders`);
  // Backend returns array; normalize to rows/count and ensure id field exists
  const rows = (response || []).map((o) => ({ id: o._id || o.id, ...o }));
  return { rows, count: rows.length };
}

const actions = {
  doFetch:
    (filter, keepPagination = false) =>
    async (dispatch) => {
      try {
        dispatch({
          type: 'ORDERS_LIST_FETCH_STARTED',
          payload: { filter, keepPagination },
        });

        const response = await list();

        dispatch({
          type: 'ORDERS_LIST_FETCH_SUCCESS',
          payload: {
            rows: response.rows,
            count: response.count,
          },
        });
      } catch (error) {
        Errors.handle(error);
        dispatch({ type: 'ORDERS_LIST_FETCH_ERROR' });
      }
    },
};

export default actions;
