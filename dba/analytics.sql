SELECT * FROM website.analytics_instances_details WHERE name="Canvas hash" AND value="fedc6d3d87f7072da922e9cace173f4584c13fcd7361eaccbc440c797943a1aa" ORDER BY detail_id desc;
SELECT * FROM website.analytics_instances_details WHERE name="Canvas hash" ORDER BY detail_id desc;
SELECT * FROM website.analytics_instances_details ORDER BY detail_id desc;
SELECT * FROM website.analytics_instances WHERE instance_id IN (316, 315, 314, 306, 297, 275, 274, 272, 270, 268, 267, 264, 261, 260, 259);
SELECT * FROM website.analytics_instances WHERE instance_id IN (SELECT instance_id FROM website.analytics_instances_details WHERE name="Canvas hash" AND value="fedc6d3d87f7072da922e9cace173f4584c13fcd7361eaccbc440c797943a1aa");
SELECT * FROM website.analytics_instances_details WHERE name="Path" AND instance_id IN (SELECT instance_id FROM website.analytics_instances_details WHERE name="Canvas hash" AND value="fedc6d3d87f7072da922e9cace173f4584c13fcd7361eaccbc440c797943a1aa");