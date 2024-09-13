import React, { useState } from "react";
import useItems from "../itemHook";
import Cards from "./card";
import "../styles/store.css";

const Store = ({ handleClick, items }) => {
  return (
    <section>
      {items.map((item) => (
        <Cards key={item.id} item={item} handleClick={handleClick} />
      ))}
    </section>
  );
};

export default Store;
