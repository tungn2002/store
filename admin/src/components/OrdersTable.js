import React, { useState, useEffect } from "react";
import { useHistory } from "react-router-dom";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableFooter,
  Badge,
  Pagination,
  Button,
} from "@windmill/react-ui";
import { EyeIcon } from "../icons";
import response from "../utils/demo/ordersData";

const OrdersTable = ({ resultsPerPage }) => {
  const history = useHistory();
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);

  // pagination setup
  const totalResults = response.length;

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // on page change, load new sliced data
  useEffect(() => {
    setData(
      response.slice((page - 1) * resultsPerPage, page * resultsPerPage)
    );
  }, [page, resultsPerPage]);

  // Get status badge type
  function getStatusBadgeType(status) {
    if (status === "pending") return "warning";
    if (status === "completed") return "success";
    return "neutral";
  }

  // Get status label
  function getStatusLabel(status) {
    if (status === "pending") return "Đang chờ";
    if (status === "completed") return "Hoàn thành";
    return status;
  }

  return (
    <div>
      {/* Table */}
      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell>ID</TableCell>
              <TableCell>Customer Email</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Total Amount</TableCell>
              <TableCell>Action</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {data.map((order) => (
              <TableRow key={order.id}>
                <TableCell>
                  <span className="font-medium">#{order.id}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{order.customer_email}</span>
                </TableCell>
                <TableCell>
                  <Badge type={getStatusBadgeType(order.status)}>
                    {getStatusLabel(order.status)}
                  </Badge>
                </TableCell>
                <TableCell>
                  <span className="font-medium">
                    ${order.total_amount.toFixed(2)}
                  </span>
                </TableCell>
                <TableCell>
                  <Button
                    icon={EyeIcon}
                    aria-label="View Details"
                    size="small"
                    onClick={() => history.push(`/app/order/${order.id}`)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TableFooter>
          <Pagination
            totalResults={totalResults}
            resultsPerPage={resultsPerPage}
            label="Table navigation"
            onChange={onPageChange}
          />
        </TableFooter>
      </TableContainer>
    </div>
  );
};

export default OrdersTable;
