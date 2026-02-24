import React, { useState } from "react";
import { Link } from "react-router-dom";
import PageTitle from "../components/Typography/PageTitle";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  Button,
} from "@windmill/react-ui";

// Sample data
const rolesData = [
  { id: 1, name: "admin", display_name: "Administrator" },
  { id: 2, name: "user", display_name: "User" },
];

const Roles = () => {
  const [roles] = useState(rolesData);

  return (
    <div>
      <PageTitle>Roles Management</PageTitle>

      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Display Name</TableCell>
              <TableCell>Actions</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {roles.map((role) => (
              <TableRow key={role.id}>
                <TableCell>
                  <span className="text-sm">{role.id}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm font-medium">{role.name}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{role.display_name}</span>
                </TableCell>
                <TableCell>
                  <Button
                    size="small"
                    tag={Link}
                    to={`/app/role-permissions/${role.id}`}
                  >
                    Permissions
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </div>
  );
};

export default Roles;
