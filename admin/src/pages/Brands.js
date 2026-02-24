import React from "react";
import PageTitle from "../components/Typography/PageTitle";
import BrandsTable from "../components/BrandsTable";

const Brands = () => {
  return (
    <div>
      <PageTitle>Brands</PageTitle>
      <BrandsTable resultsPerPage={10} />
    </div>
  );
};

export default Brands;
