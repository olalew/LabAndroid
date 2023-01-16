import { Component, NgZone } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  public gifts: string[] = []
  public giftInEdition: string = "Default Gift"

  constructor(private zone: NgZone) {
    this.readList();

    (window as any).activity.onDefaultSet = (str: string) => {
      console.log("location");
      console.log(str)

      this.zone.run(() => {
        this.giftInEdition = str
      })
    }
  }

  ngOnInit(): void {
    this.giftInEdition = this.getActivity().defaultGift()
  }

  public appendToList() {
    this.getActivity().appendGift(this.giftInEdition);
    this.giftInEdition = "Default Gift";
    this.readList();
  }

  private readList() {
    let giftString = this.getActivity().getPresentList();
    this.gifts = JSON.parse(!!giftString ? giftString: "[]");
  }

  public generate() : void {
    this.getActivity().generate()
  }

  private getActivity(): Activity {
    if (!!(window as any).activity) {
      return (window as any).activity as Activity
    }

    let activity = new Activity()
    activity.appendGift = (gift) => {
      this.gifts.push(gift);
    };
    activity.getPresentList = () => {
      return JSON.stringify(this.gifts)
    }
    return activity
  }

  public askGoogle() {
    this.getActivity().askGoogle()
  }
}

export class Activity {
  public getPresentList: () => string | undefined = () => "";
  public appendGift: (gift: string) => void = (gift: String) => {};
  public generate: () => void = () => {}
  public askGoogle: () => void = () => {}
  public defaultGift: () => string = () => ""
}

/*

      (window as any).activity.onReceiveLocation = (location: string) => {
      console.log("location");
      console.log(location)

      this.zone.run(() => {
        this.location = location
      })

*/
