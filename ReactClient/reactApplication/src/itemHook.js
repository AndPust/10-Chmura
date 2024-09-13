import React, { useState, useEffect } from "react";

const useItems = () => {
  const [items, itemsSet] = useState([]);

  useEffect(() => {

    async function fetchItems() {
      const fullResponse = await fetch('http://localhost:9000/api', {
        headers: {"key": "1"}
      });
      const responseJson = await fullResponse.json();
      itemsSet(responseJson);
    }
    fetchItems();
  }, []);

  return [items];
};

export default useItems;