import React from "react";
import PageTitle from "../components/Typography/PageTitle";
import CategoriesTable from "../components/CategoriesTable";

const Categories = () => {
  return (
    <div>
      <PageTitle>Categories</PageTitle>
      <CategoriesTable resultsPerPage={10} />
    </div>
  );
};

export default Categories;
