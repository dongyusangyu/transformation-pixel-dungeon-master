import unittest

from server.cloud_backend import TalentCloudStore


class CloudHeroHallMergeTest(unittest.TestCase):

    def test_incoming_hero_hall_is_uploaded_without_ranking_limit(self):
        hall = [{"gameID": f"hall-{index}", "score": index} for index in range(25)]

        merged = TalentCloudStore._merge_rankings({}, {"hero_hall_records": hall})

        self.assertEqual(25, len(merged["hero_hall_records"]))
        self.assertEqual("hall-0", merged["hero_hall_records"][0]["gameID"])

    def test_legacy_upload_without_hero_hall_keeps_server_records(self):
        existing = {"hero_hall_records": [{"gameID": "saved", "score": 100}]}

        merged = TalentCloudStore._merge_rankings(existing, {"records": []})

        self.assertEqual("saved", merged["hero_hall_records"][0]["gameID"])

    def test_explicit_empty_hero_hall_removes_server_records(self):
        existing = {"hero_hall_records": [{"gameID": "removed", "score": 100}]}

        merged = TalentCloudStore._merge_rankings(existing, {"hero_hall_records": []})

        self.assertEqual([], merged["hero_hall_records"])


if __name__ == "__main__":
    unittest.main()
