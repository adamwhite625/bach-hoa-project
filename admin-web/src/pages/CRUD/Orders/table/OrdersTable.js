import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { DataGrid } from '@mui/x-data-grid';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Widget from 'components/Widget';
import actions from 'actions/orders/ordersListActions';

const OrdersTable = () => {
  const dispatch = useDispatch();
  const rows = useSelector((store) => store.orders.list.rows);
  const loading = useSelector((store) => store.orders.list.loading);

  React.useEffect(() => {
    dispatch(actions.doFetch({}));
  }, [dispatch]);

  function NoRowsOverlay() {
    return (
      <Stack height='100%' alignItems='center' justifyContent='center'>
        No orders
      </Stack>
    );
  }

  const columns = [
    { field: 'id', headerName: 'ID', flex: 1, minWidth: 220 },
    { field: 'status', headerName: 'Status', flex: 0.6 },
    { field: 'totalPrice', headerName: 'Total', flex: 0.5 },
    { field: 'isPaid', headerName: 'Paid', flex: 0.4, valueGetter: (p) => (p.row.isPaid ? 'Yes' : 'No') },
    { field: 'isDelivered', headerName: 'Delivered', flex: 0.6, valueGetter: (p) => (p.row.isDelivered ? 'Yes' : 'No') },
  ];

  return (
    <Widget title='Orders' disableWidgetMenu>
      <div style={{ minHeight: 500, width: '100%', paddingTop: 20, paddingBottom: 20 }}>
        <DataGrid
          rows={loading ? [] : rows}
          columns={columns}
          components={{ NoRowsOverlay, LoadingOverlay: LinearProgress }}
          loading={loading}
          autoHeight
          pageSize={10}
          rowsPerPageOptions={[10, 20, 50]}
        />
      </div>
    </Widget>
  );
};

export default OrdersTable;
