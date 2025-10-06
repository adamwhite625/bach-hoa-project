import React from 'react';
import { DataGrid } from '@mui/x-data-grid';
import LinearProgress from '@mui/material/LinearProgress';
import Stack from '@mui/material/Stack';
import Widget from 'components/Widget';

const mockOrders = [
  { id: 'ORD-1001', customer: 'Nguyen Van A', status: 'Pending', totalPrice: 129000, isPaid: false, isDelivered: false, createdAt: '2025-10-01' },
  { id: 'ORD-1002', customer: 'Tran Thi B', status: 'Processing', totalPrice: 259000, isPaid: true, isDelivered: false, createdAt: '2025-10-02' },
  { id: 'ORD-1003', customer: 'Le Van C', status: 'Delivered', totalPrice: 99000, isPaid: true, isDelivered: true, createdAt: '2025-10-03' },
  { id: 'ORD-1004', customer: 'Pham D', status: 'Cancelled', totalPrice: 150000, isPaid: false, isDelivered: false, createdAt: '2025-10-04' },
];

const OrdersTable = () => {
  function NoRowsOverlay() {
    return (
      <Stack height='100%' alignItems='center' justifyContent='center'>
        No orders
      </Stack>
    );
  }

  const columns = [
    { field: 'id', headerName: 'Order ID', flex: 1, minWidth: 180 },
    { field: 'customer', headerName: 'Customer', flex: 0.8 },
    { field: 'status', headerName: 'Status', flex: 0.6 },
    { field: 'totalPrice', headerName: 'Total (VND)', flex: 0.6, valueFormatter: ({ value }) => new Intl.NumberFormat('vi-VN').format(value) },
    { field: 'isPaid', headerName: 'Paid', flex: 0.4, valueGetter: (p) => (p.row.isPaid ? 'Yes' : 'No') },
    { field: 'isDelivered', headerName: 'Delivered', flex: 0.6, valueGetter: (p) => (p.row.isDelivered ? 'Yes' : 'No') },
    { field: 'createdAt', headerName: 'Created At', flex: 0.7 },
  ];

  return (
    <Widget title='Orders' disableWidgetMenu>
      <div style={{ minHeight: 500, width: '100%', paddingTop: 20, paddingBottom: 20 }}>
        <DataGrid
          rows={mockOrders}
          columns={columns}
          components={{ NoRowsOverlay, LoadingOverlay: LinearProgress }}
          autoHeight
          pageSize={10}
          rowsPerPageOptions={[5, 10, 20]}
          disableSelectionOnClick
          disableColumnMenu
        />
      </div>
    </Widget>
  );
};

export default OrdersTable;
