# !/bin/sh

RESULTS_CSV=data/results-breaking.csv
FORK_ORG=breakbot-playground

while IFS=, read -r owner name number
do
	if [[ ! -d "$name" ]]; then
		# Clone
		echo "Cloning $owner/$name"
		gh repo fork --org $FORK_ORG --clone $owner/$name
		cd $name

		# Config
		echo "Creating breakbot.yml config"
		mkdir -p .github
		cat <<EOF > .github/breakbot.yml
clients:
  top: 100
  stars: 5
EOF
		git add .github/breakbot.yml
		git commit -m ".github/breakbot.yml"
		git push
		cd ..
	fi

	prs=$(gh pr list --repo $owner/$name --limit 1000 --state all | grep "^$number" | cut -f1-3)	

	if [[ ! -z "$prs" ]]; then
		echo "Forking PR#$number"
		IFS=$'\t' read -r prId prTitle prHead <<< $prs
		gh pr create --repo "$FORK_ORG/$name" --title "$prTitle" --head "$prHead" --body "Copy of $owner/$name:$number" --no-maintainer-edit
	else
		echo "Couldn't find PR#$number"
	fi
done < $RESULTS_CSV

