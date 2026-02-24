import React, { useState } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Input } from "@windmill/react-ui";
import UsersTable from "../components/UsersTable";

const Customers = () => {
  const [filter, setFilter] = useState("");

  return (
    <div>
      <PageTitle>Manage Customers</PageTitle>

      {/* Search Input */}
      <div className="mb-6">
        <Input
          type="text"
          placeholder="Search by email..."
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          aria-label="Search by email"
        />
      </div>

      <UsersTable resultsPerPage={10} filter={filter} />
    </div>
  );
};

export default Customers;
